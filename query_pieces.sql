
-- For the purpose of writing out and testing these queries, there are some dummy values here
-- namely the quantity ranges and the character_id of 1

-- The whole shebang
SELECT * FROM 
    (SELECT * FROM spells
    INNER JOIN 
        (SELECT source_id FROM character_sources WHERE character_id = 1) AS cs
        ON spells.source_id = cs.source_id ) 
    AS r1
    INNER JOIN
    (SELECT id FROM spells
        INNER JOIN (SELECT spell_id FROM spell_classes
            INNER JOIN (SELECT class_id FROM character_classes WHERE character_id = 1) AS cci ON spell_classes.class_id = cci.class_id GROUP BY spell_id) AS scci ON spells.id = scci.spell_id) AS r2
        ON r1.id = r2.id
    


----- Getting other IDs

-- Select the currently visible classes
SELECT class_id FROM character_classes WHERE character_id = 1;

-- Select the currently visible sources
SELECT source_id FROM character_sources WHERE character_id = 1;

-- Select the currently visible schools
SELECT school_id FROM character_schools WHERE character_id = 1;


----- Getting spell IDs from these
--- The difference between sources and the other two is that spells have a 1-1 relationship with sources and schools, but a 1-many with the classes

-- Select the spell ids that fit with the current classes
SELECT spell_id FROM spell_classes INNER JOIN (SELECT class_id FROM character_classes WHERE character_id = 1) AS scci ON scci.class_id = spell_classes.class_id GROUP BY spell_id;

-- Select the spell ids that fit with the current schools
SELECT id from spells INNER JOIN (SELECT school_id FROM character_schools WHERE character_id = 1) AS cs on spells.school_id = cs.school_id

-- Select the spell ids that fit with the current sources
SELECT id FROM spells INNER JOIN (SELECT source_id FROM character_sources WHERE character_id = 1) AS cs ON spells.source_id = cs.source_id;


--- Now we want to combine all of these spell ids
SELECT spell_id FROM
    (SELECT spell_id FROM spell_classes INNER JOIN (SELECT class_id FROM character_classes WHERE character_id = 1) AS scci ON scci.class_id = spell_classes.class_id GROUP BY spell_id) AS from_classes
    INNER JOIN
    (SELECT id from spells INNER JOIN (SELECT school_id FROM character_schools WHERE character_id = 1) AS cs on spells.school_id = cs.school_id) AS from_schools ON from_classes.spell_id = from_schools.id
    INNER JOIN
    (SELECT id FROM spells INNER JOIN (SELECT source_id FROM character_sources WHERE character_id = 1) AS cs ON spells.source_id = cs.source_id) AS from_sources ON from_classes.spell_id = from_sources.id;

--- We also (might) want to check a spell list
--- The piece to get the IDs of spells from a specific list (in this example, favorites), is
SELECT spell_id FROM character_spells WHERE character_id = 1 AND favorite = 1;


--- Add the filtering conditions
--- and obviously a sort field
-- The last join is optional - only need it if we're looking at a particular spell list
SELECT * FROM spells
    INNER JOIN
    (SELECT spell_id FROM spell_classes INNER JOIN (SELECT class_id FROM character_classes WHERE character_id = 1) AS scci ON scci.class_id = spell_classes.class_id GROUP BY spell_id) AS from_classes ON spells.id = from_classes.spell_id
    INNER JOIN
    (SELECT id from spells INNER JOIN (SELECT school_id FROM character_schools WHERE character_id = 1) AS cs on spells.school_id = cs.school_id) AS from_schools ON from_classes.spell_id = from_schools.id
    INNER JOIN
    (SELECT id FROM spells INNER JOIN (SELECT source_id FROM character_sources WHERE character_id = 1) AS cs ON spells.source_id = cs.source_id) AS from_sources ON from_classes.spell_id = from_sources.id
    INNER JOIN
    (SELECT spell_id FROM character_spells WHERE character_id = 1 AND favorite = 1) AS from_character ON spells.id = from_character.spell_id
WHERE (casting_time_base_value BETWEEN 0 AND 10000) AND (duration_base_value BETWEEN 0 AND 10000) AND (range_base_value BETWEEN 0 AND 10000) ORDER BY name;