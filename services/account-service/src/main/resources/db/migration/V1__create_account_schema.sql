create table accounts (
    id uuid primary key,
    account_number varchar(32) not null unique,
    owner_subject varchar(120) not null,
    currency varchar(3) not null,
    product_type varchar(40) not null,
    status varchar(40) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null,
    constraint chk_accounts_currency_upper check (currency = upper(currency)),
    constraint chk_accounts_status check (status in ('ACTIVE', 'FROZEN')),
    constraint chk_accounts_product_type check (product_type in ('CHECKING', 'SAVINGS', 'SYSTEM'))
);

create table ledger_entries (
    id uuid primary key,
    account_id uuid not null references accounts(id),
    entry_group_id uuid not null,
    signed_amount numeric(19, 2) not null,
    currency varchar(3) not null,
    entry_type varchar(40) not null,
    memo varchar(240),
    created_at timestamptz not null,
    constraint chk_ledger_currency_upper check (currency = upper(currency)),
    constraint chk_ledger_non_zero_amount check (signed_amount <> 0),
    constraint chk_ledger_entry_type check (entry_type in ('ACCOUNT_OPENING', 'TRANSFER_DEBIT', 'TRANSFER_CREDIT', 'STATUS_CHANGE'))
);

create index idx_ledger_entries_account_created on ledger_entries(account_id, created_at desc);
create index idx_ledger_entries_group on ledger_entries(entry_group_id);

create table account_balance_snapshots (
    account_id uuid primary key references accounts(id) on delete cascade,
    currency varchar(3) not null,
    ledger_balance numeric(19, 2) not null,
    available_balance numeric(19, 2) not null,
    updated_at timestamptz not null,
    version bigint not null,
    constraint chk_balance_currency_upper check (currency = upper(currency))
);

create table idempotency_records (
    id uuid primary key,
    idempotency_key varchar(160) not null unique,
    operation varchar(80) not null,
    request_hash varchar(64) not null,
    response_status integer not null,
    response_body text not null,
    created_at timestamptz not null,
    expires_at timestamptz not null,
    version bigint not null
);

create index idx_idempotency_records_expires_at on idempotency_records(expires_at);

create or replace function reject_ledger_entry_mutation()
returns trigger as $$
begin
    raise exception 'ledger_entries are immutable';
end;
$$ language plpgsql;

create trigger ledger_entries_no_update
before update on ledger_entries
for each row execute function reject_ledger_entry_mutation();

create trigger ledger_entries_no_delete
before delete on ledger_entries
for each row execute function reject_ledger_entry_mutation();

insert into accounts (
    id,
    account_number,
    owner_subject,
    currency,
    product_type,
    status,
    created_at,
    updated_at,
    version
) values (
    '00000000-0000-0000-0000-000000000001',
    'NBX-SYSTEM-OPENING',
    'system',
    'USD',
    'SYSTEM',
    'ACTIVE',
    now(),
    now(),
    0
);

insert into account_balance_snapshots (
    account_id,
    currency,
    ledger_balance,
    available_balance,
    updated_at,
    version
) values (
    '00000000-0000-0000-0000-000000000001',
    'USD',
    0.00,
    0.00,
    now(),
    0
);

