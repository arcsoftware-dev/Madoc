UPDATE madoc.gamesheets
SET json_string = jsonb_set(
        jsonb_set(
                json_string,
                '{awayAttendanceByPlayerId}',
                (
                    SELECT jsonb_agg(
                                   jsonb_set(obj, '{rosterId}', to_jsonb(ra.id))
                           )
                    FROM jsonb_array_elements(json_string->'awayAttendanceByPlayerId') obj
                             LEFT JOIN madoc.roster_assignments ra
                                       ON (obj->>'playerId')::int = ra.player_id
                )
        ),
        '{homeAttendanceByPlayerId}',
        (
            SELECT jsonb_agg(
                           jsonb_set(obj, '{rosterId}', to_jsonb(ra.id))
                   )
            FROM jsonb_array_elements(json_string->'homeAttendanceByPlayerId') obj
                     LEFT JOIN madoc.roster_assignments ra
                               ON (obj->>'playerId')::int = ra.player_id
        )
                  )
WHERE jsonb_typeof(json_string->'awayAttendanceByPlayerId') = 'array'
  AND jsonb_typeof(json_string->'homeAttendanceByPlayerId') = 'array';


UPDATE madoc.gamesheets
SET json_string = jsonb_set(
        jsonb_set(
                json_string,
                '{awayAttendanceByPlayerId}',
                (
                    SELECT jsonb_agg(
                                   obj - 'playerId'
                           )
                    FROM jsonb_array_elements(json_string->'awayAttendanceByPlayerId') obj
                )
        ),
        '{homeAttendanceByPlayerId}',
        (
            SELECT jsonb_agg(
                           obj - 'playerId'
                   )
            FROM jsonb_array_elements(json_string->'homeAttendanceByPlayerId') obj
        )
                  )
WHERE jsonb_typeof(json_string->'awayAttendanceByPlayerId') = 'array'
  AND jsonb_typeof(json_string->'homeAttendanceByPlayerId') = 'array';