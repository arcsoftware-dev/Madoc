-- update goals
ALTER TABLE madoc.goals DROP CONSTRAINT goals_player_id_fkey;
ALTER TABLE madoc.goals DROP CONSTRAINT goals_primary_assist_id_fkey;
ALTER TABLE madoc.goals DROP CONSTRAINT goals_secondary_assist_id_fkey;

UPDATE madoc.goals go
SET player_id = ra.id
FROM madoc.roster_assignments ra
WHERE go.player_id = ra.player_id;

-- update primary assists
UPDATE madoc.goals go
SET primary_assist_id = assist_map.roster_id
FROM (
    SELECT a.player_id AS player_id, ra.id AS roster_id, a.id AS assist_id FROM madoc.assists a
        JOIN madoc.roster_assignments ra ON a.player_id = ra.player_id) assist_map
WHERE go.primary_assist_id = assist_map.assist_id;

-- update secondary assists
UPDATE madoc.goals go
SET secondary_assist_id = assist_map.roster_id
FROM (
    SELECT a.player_id AS player_id, ra.id AS roster_id, a.id AS assist_id FROM madoc.assists a
        JOIN madoc.roster_assignments ra ON a.player_id = ra.player_id) assist_map
WHERE go.secondary_assist_id = assist_map.assist_id;

-- alter table to rename columns and add constraints
ALTER TABLE madoc.goals RENAME COLUMN player_id TO scorer_roster_assignment_id;
ALTER TABLE madoc.goals RENAME COLUMN primary_assist_id TO primary_assist_roster_assignment_id;
ALTER TABLE madoc.goals RENAME COLUMN secondary_assist_id TO secondary_assist_roster_assignment_id;

ALTER TABLE madoc.goals
    ADD CONSTRAINT goals_scorer_roster_assignment_id_fkey
        FOREIGN KEY (scorer_roster_assignment_id)
            REFERENCES madoc.roster_assignments(id);
ALTER TABLE madoc.goals
    ADD CONSTRAINT goals_primary_assist_roster_assignment_id_id_fkey
        FOREIGN KEY (primary_assist_roster_assignment_id)
            REFERENCES madoc.roster_assignments(id);
ALTER TABLE madoc.goals
    ADD CONSTRAINT goals_secondary_assist_roster_assignment_id_fkey
        FOREIGN KEY (secondary_assist_roster_assignment_id)
            REFERENCES madoc.roster_assignments(id);

-- drop assists table
DROP TABLE madoc.assists;