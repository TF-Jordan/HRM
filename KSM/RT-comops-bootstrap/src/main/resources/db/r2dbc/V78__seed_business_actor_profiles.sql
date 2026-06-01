-- V078: Seed missing business_actor_profile rows for demo actors.
--
-- The V068 seed created actor rows and linked the organization to actor
-- 00000000-0000-0000-0000-0000000b0001 (super.admin) via business_actor_id,
-- but never inserted the corresponding business_actor_profile rows.
--
-- ActorCoreCurrentBusinessActorProvider.getCurrentBusinessActorId() calls
-- businessActorProfileRepository.findByActorId() then returns profile.id().
-- That returned id is then used to query organization.business_actor_id.
-- Therefore the super.admin profile id is set to 0000000b0001 — the same value
-- already stored in organization.business_actor_id — so ownership resolves correctly.
--
-- Idempotent: ON CONFLICT DO NOTHING.

INSERT INTO actor.business_actor_profile (
    id,
    tenant_id,
    created_at,
    updated_at,
    actor_id,
    name,
    governance_status,
    is_individual,
    is_available,
    is_verified,
    is_active,
    type,
    role,
    qualifications,
    payment_methods,
    addresses
) VALUES
    -- super.admin — owns organization 0000000a0002; profile id MUST equal business_actor_id stored there
    (
        '00000000-0000-0000-0000-0000000b0001',
        '00000000-0000-0000-0000-0000000a0001',
        now(), now(),
        '00000000-0000-0000-0000-0000000b0001',
        'Jordan Toulépi',
        'APPROVED',
        false, true, true, true,
        'BUSINESS_ACTOR', 'OWNER',
        ARRAY[]::text[], ARRAY[]::text[], ARRAY[]::uuid[]
    ),
    -- hr.admin
    (
        '00000000-0000-0000-0000-0000000ba002',
        '00000000-0000-0000-0000-0000000a0001',
        now(), now(),
        '00000000-0000-0000-0000-0000000b0002',
        'Marie Ngo',
        'APPROVED',
        false, true, true, true,
        'BUSINESS_ACTOR', 'MEMBER',
        ARRAY[]::text[], ARRAY[]::text[], ARRAY[]::uuid[]
    ),
    -- employee
    (
        '00000000-0000-0000-0000-0000000ba003',
        '00000000-0000-0000-0000-0000000a0001',
        now(), now(),
        '00000000-0000-0000-0000-0000000b0003',
        'Jean Dupont',
        'APPROVED',
        false, true, true, true,
        'BUSINESS_ACTOR', 'MEMBER',
        ARRAY[]::text[], ARRAY[]::text[], ARRAY[]::uuid[]
    )
ON CONFLICT DO NOTHING;
