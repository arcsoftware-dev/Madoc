-- update penalties
ALTER TABLE madoc.penalties DROP CONSTRAINT penalties_player_id_fkey;

UPDATE madoc.penalties p
SET player_id = ra.id
FROM madoc.roster_assignments ra
WHERE p.player_id = ra.player_id;

-- alter table to rename columns and add constraints
ALTER TABLE madoc.penalties RENAME COLUMN player_id TO roster_assignment_id;

ALTER TABLE madoc.penalties
    ADD CONSTRAINT penalties_roster_assignment_id_fkey
        FOREIGN KEY (roster_assignment_id)
            REFERENCES madoc.roster_assignments(id);