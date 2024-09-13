CREATE TABLE IF NOT EXISTS items (
   id SERIAL PRIMARY KEY,
   name VARCHAR(255) NOT NULL,
   done BOOLEAN DEFAULT FALSE,
   quantity INTEGER DEFAULT 1
);