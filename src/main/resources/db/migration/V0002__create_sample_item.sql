-- sample enrichment: the SampleItem child entity of the Sample aggregate
CREATE TABLE IF NOT EXISTS sample_item (
    id UUID PRIMARY KEY,
    label VARCHAR(255) NOT NULL,
    checked BOOLEAN NOT NULL,
    -- join column of the unidirectional Sample.items one-to-many; nullable, because Hibernate may write it in a
    -- separate UPDATE after inserting the row
    items_id UUID,
    FOREIGN KEY (items_id) REFERENCES sample(id)
    );
