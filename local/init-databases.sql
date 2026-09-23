-- Creates one database + user per service (mirrors k8s/stateful/pgvector.yaml)
CREATE USER account_user WITH PASSWORD 'account_pass';
CREATE USER workspace_user WITH PASSWORD 'workspace_pass';
CREATE USER intelligence_user WITH PASSWORD 'intelligence_pass';

CREATE DATABASE account_db OWNER account_user;
CREATE DATABASE workspace_db OWNER workspace_user;
CREATE DATABASE intelligence_db OWNER intelligence_user;
