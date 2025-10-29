ALTER TABLE madoc.attendance ADD COLUMN roster_assignment_id integer;
ALTER TABLE madoc.attendance DROP CONSTRAINT unique_player_game_CONSTRAINT;
ALTER TABLE madoc.attendance DROP CONSTRAINT attendance_player_id_fkey;
ALTER TABLE madoc.attendance DROP CONSTRAINT attendance_team_id_fkey;
ALTER TABLE madoc.attendance
    ADD CONSTRAINT unique_player_game_CONSTRAINT
        UNIQUE (game_id, roster_assignment_id);

UPDATE madoc.attendance a
SET roster_assignment_id = ra.id
FROM madoc.roster_assignments ra
WHERE a.player_id = ra.player_id;

ALTER TABLE madoc.attendance DROP COLUMN player_id;
ALTER TABLE madoc.attendance DROP COLUMN  team_id;
ALTER TABLE madoc.attendance
    ADD CONSTRAINT attendance_roster_assignment_id_fkey
        FOREIGN KEY (roster_assignment_id)
            REFERENCES madoc.roster_assignments(id);