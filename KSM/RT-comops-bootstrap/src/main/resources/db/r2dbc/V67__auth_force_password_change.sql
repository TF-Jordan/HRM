-- Adds a flag indicating that the user must change their password at next login.
-- Set to TRUE when an admin creates a user with a temporary password; cleared
-- automatically by UserAccount.updatePassword() / AuthApplicationService.resetPassword().

ALTER TABLE auth_core.user_account
    ADD COLUMN IF NOT EXISTS force_password_change boolean NOT NULL DEFAULT false;
