--liquibase formatted sql

--changeset hub:014-file-uploads-schema
CREATE TABLE file_uploads (
    file_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(255) NOT NULL,
    usecase VARCHAR(255) NOT NULL,
    bucket_name VARCHAR(255) NOT NULL,
    object_path VARCHAR(500) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    uploaded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    mime_type VARCHAR(255) NOT NULL,
    UNIQUE(object_path)
);

-- Create index for faster lookups
CREATE INDEX idx_file_uploads_user_id ON file_uploads(user_id);
CREATE INDEX idx_file_uploads_usecase ON file_uploads(usecase);
CREATE INDEX idx_file_uploads_bucket_name ON file_uploads(bucket_name);