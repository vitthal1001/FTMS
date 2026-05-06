create table auth_users (
    id uuid primary key,
    email varchar(320) not null unique,
    full_name varchar(160) not null,
    password_hash varchar(100) not null,
    enabled boolean not null,
    failed_login_attempts integer not null,
    locked_until timestamptz,
    last_login_at timestamptz,
    version bigint not null
);

create table auth_user_roles (
    user_id uuid not null references auth_users(id) on delete cascade,
    role varchar(40) not null,
    primary key (user_id, role)
);

create table auth_refresh_tokens (
    id uuid primary key,
    user_id uuid not null references auth_users(id) on delete cascade,
    family_id uuid not null,
    token_hash varchar(64) not null unique,
    issued_at timestamptz not null,
    expires_at timestamptz not null,
    used_at timestamptz,
    revoked_at timestamptz,
    version bigint not null
);

create index idx_auth_refresh_tokens_family_id on auth_refresh_tokens(family_id);
create index idx_auth_refresh_tokens_user_family on auth_refresh_tokens(user_id, family_id);
create index idx_auth_refresh_tokens_expires_at on auth_refresh_tokens(expires_at);

