ALTER TABLE dragon DROP CONSTRAINT IF EXISTS dragon_id_positive;
ALTER TABLE dragon ADD CONSTRAINT dragon_id_positive CHECK (id > 0);
ALTER TABLE dragon DROP CONSTRAINT IF EXISTS dragon_name_not_blank;
ALTER TABLE dragon ADD CONSTRAINT dragon_name_not_blank CHECK (btrim(name) <> '');
ALTER TABLE dragon DROP CONSTRAINT IF EXISTS dragon_age_positive;
ALTER TABLE dragon ADD CONSTRAINT dragon_age_positive CHECK (age > 0);
ALTER TABLE dragon DROP CONSTRAINT IF EXISTS dragon_wingspan_positive;
ALTER TABLE dragon ADD CONSTRAINT dragon_wingspan_positive CHECK (wingspan > 0);
ALTER TABLE dragon DROP CONSTRAINT IF EXISTS dragon_weight_positive;
ALTER TABLE dragon ADD CONSTRAINT dragon_weight_positive CHECK (weight > 0);
ALTER TABLE dragon DROP CONSTRAINT IF EXISTS dragon_character_known;
ALTER TABLE dragon ADD CONSTRAINT dragon_character_known CHECK (dragon_character IS NULL OR dragon_character IN ('WISE', 'GOOD', 'CHAOTIC', 'FICKLE'));
ALTER TABLE dragon ALTER COLUMN creation_date SET DEFAULT CURRENT_DATE;

ALTER TABLE coordinates DROP CONSTRAINT IF EXISTS coordinates_id_positive;
ALTER TABLE coordinates ADD CONSTRAINT coordinates_id_positive CHECK (id > 0);

ALTER TABLE dragon_cave DROP CONSTRAINT IF EXISTS dragon_cave_id_positive;
ALTER TABLE dragon_cave ADD CONSTRAINT dragon_cave_id_positive CHECK (id > 0);
ALTER TABLE dragon_cave DROP CONSTRAINT IF EXISTS dragon_cave_treasures_positive;
ALTER TABLE dragon_cave ADD CONSTRAINT dragon_cave_treasures_positive CHECK (number_of_treasures > 0);

ALTER TABLE person DROP CONSTRAINT IF EXISTS person_id_positive;
ALTER TABLE person ADD CONSTRAINT person_id_positive CHECK (id > 0);
ALTER TABLE person DROP CONSTRAINT IF EXISTS person_name_not_blank;
ALTER TABLE person ADD CONSTRAINT person_name_not_blank CHECK (btrim(name) <> '');
ALTER TABLE person DROP CONSTRAINT IF EXISTS person_eye_known;
ALTER TABLE person ADD CONSTRAINT person_eye_known CHECK (eye_color IN ('GREEN', 'BLACK', 'BLUE', 'YELLOW', 'ORANGE'));
ALTER TABLE person DROP CONSTRAINT IF EXISTS person_hair_known;
ALTER TABLE person ADD CONSTRAINT person_hair_known CHECK (hair_color IS NULL OR hair_color IN ('GREEN', 'BLACK', 'BLUE', 'YELLOW', 'ORANGE'));
ALTER TABLE person DROP CONSTRAINT IF EXISTS person_country_known;
ALTER TABLE person ADD CONSTRAINT person_country_known CHECK (nationality IS NULL OR nationality IN ('RUSSIA', 'USA', 'GERMANY', 'SPAIN', 'INDIA'));

ALTER TABLE dragon_head DROP CONSTRAINT IF EXISTS dragon_head_id_positive;
ALTER TABLE dragon_head ADD CONSTRAINT dragon_head_id_positive CHECK (id > 0);

ALTER TABLE location DROP CONSTRAINT IF EXISTS location_id_positive;
ALTER TABLE location ADD CONSTRAINT location_id_positive CHECK (id > 0);
ALTER TABLE location DROP CONSTRAINT IF EXISTS location_name_length;
ALTER TABLE location ADD CONSTRAINT location_name_length CHECK (name IS NULL OR char_length(name) <= 321);

ALTER TABLE killer_team DROP CONSTRAINT IF EXISTS killer_team_id_positive;
ALTER TABLE killer_team ADD CONSTRAINT killer_team_id_positive CHECK (id > 0);
ALTER TABLE killer_team DROP CONSTRAINT IF EXISTS killer_team_name_not_blank;
ALTER TABLE killer_team ADD CONSTRAINT killer_team_name_not_blank CHECK (btrim(name) <> '');

CREATE OR REPLACE FUNCTION dragon_with_max_creation_date()
RETURNS integer AS $$
  SELECT dragon.id FROM dragon ORDER BY dragon.creation_date DESC, dragon.id DESC LIMIT 1;
$$ LANGUAGE sql;

CREATE OR REPLACE FUNCTION dragons_by_name_part(part text)
RETURNS TABLE(id integer) AS $$
  SELECT dragon.id FROM dragon WHERE strpos(dragon.name, part) > 0 ORDER BY dragon.id;
$$ LANGUAGE sql;

CREATE OR REPLACE FUNCTION dragons_by_character_less_than(ch text)
RETURNS TABLE(id integer) AS $$
  SELECT dragon.id
  FROM dragon
  WHERE dragon.dragon_character IS NOT NULL
    AND CASE dragon.dragon_character
          WHEN 'WISE' THEN 0
          WHEN 'GOOD' THEN 1
          WHEN 'CHAOTIC' THEN 2
          WHEN 'FICKLE' THEN 3
          ELSE 99
        END
      <
        CASE ch
          WHEN 'WISE' THEN 0
          WHEN 'GOOD' THEN 1
          WHEN 'CHAOTIC' THEN 2
          WHEN 'FICKLE' THEN 3
          ELSE -1
        END
  ORDER BY dragon.id;
$$ LANGUAGE sql;

CREATE OR REPLACE FUNCTION create_killer_team(team_name text, member_ids text)
RETURNS integer AS $$
DECLARE
  new_id integer;
  mid_text text;
BEGIN
  IF team_name IS NULL OR btrim(team_name) = '' THEN
    RAISE EXCEPTION 'Имя команды не может быть пустым';
  END IF;
  INSERT INTO killer_team(name) VALUES (btrim(team_name)) RETURNING id INTO new_id;
  IF member_ids IS NOT NULL AND btrim(member_ids) <> '' THEN
    FOREACH mid_text IN ARRAY string_to_array(member_ids, ',')
    LOOP
      INSERT INTO killer_team_member(team_id, person_id) VALUES (new_id, CAST(btrim(mid_text) AS integer));
    END LOOP;
  END IF;
  RETURN new_id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION send_killer_team_to_cave(p_team_id integer, p_cave_id integer)
RETURNS integer AS $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM killer_team WHERE id = p_team_id) THEN
    RAISE EXCEPTION 'Команда не найдена';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM dragon_cave WHERE id = p_cave_id) THEN
    RAISE EXCEPTION 'Пещера не найдена';
  END IF;
  UPDATE killer_team SET cave_id = p_cave_id WHERE id = p_team_id;
  RETURN p_team_id;
END;
$$ LANGUAGE plpgsql;
