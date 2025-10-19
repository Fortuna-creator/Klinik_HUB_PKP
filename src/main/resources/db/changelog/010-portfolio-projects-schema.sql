--liquibase formatted sql

--changeset hub:10
-- Add portfolio_projects table for managing individual portfolio projects

-- Create portfolio_projects table
CREATE TABLE IF NOT EXISTS portfolio_projects (
    id BIGSERIAL PRIMARY KEY,
    portfolio_id BIGINT NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    project_name VARCHAR(255) NOT NULL,
    price DECIMAL(19,2),
    detail_description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create portfolio_project_images table for storing multiple image URLs
CREATE TABLE IF NOT EXISTS portfolio_project_images (
    portfolio_project_id BIGINT NOT NULL REFERENCES portfolio_projects(id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    PRIMARY KEY (portfolio_project_id, image_url)
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_portfolio_projects_portfolio_id ON portfolio_projects(portfolio_id);
CREATE INDEX IF NOT EXISTS idx_portfolio_project_images_project_id ON portfolio_project_images(portfolio_project_id);

--rollback DROP TABLE IF EXISTS portfolio_project_images;
--rollback DROP TABLE IF EXISTS portfolio_projects;