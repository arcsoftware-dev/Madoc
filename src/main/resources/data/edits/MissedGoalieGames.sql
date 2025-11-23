--Avalanche
update madoc.attendance
set attended = false
where roster_assignment_id = 15
  and game_id in (14, 20);

--Golden Knights
update madoc.attendance
set attended = false
where roster_assignment_id = 89
  and game_id in (8, 12, 18, 24, 25);

--Redwings
update madoc.attendance
set attended = false
where roster_assignment_id = 74
  and game_id in (9);

--Blackhawks
update madoc.attendance
set attended = false
where roster_assignment_id = 29
  and game_id in (11);

--Leafs
update madoc.attendance
set attended = false
where roster_assignment_id = 59
  and game_id in (15);

--Canucks
update madoc.attendance
set attended = false
where roster_assignment_id = 44
  and game_id in (23);