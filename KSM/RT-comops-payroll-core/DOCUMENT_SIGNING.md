# Document signing keys for `RT-comops-payroll-core`

Payslips, soldes de tout compte and work certificates produced by payroll-core
are sealed with an **RSA-2048 / SHA-256** electronic signature. The seal is:

- a detached signature over the document's *canonical content*,
- stored on the `payroll_document` row (algorithm, content hash, signature, key id),
- printed on the PDF (verification code, hash, algorithm, signature date),
- re-verifiable any time via `GET /api/v1/payroll/documents/{id}/verify`.

## Where the key comes from

The signing key is provided by `RsaDocumentSigningKeyProvider`, configured in
`bootstrap`'s `application.yml`:

```yaml
iwm:
  payroll:
    document-signing:
      key-id:           ${IWM_PAYROLL_DOC_SIGNING_KEY_ID:payroll-doc-key-1}
      private-key-path: ${IWM_PAYROLL_DOC_SIGNING_PRIVATE_KEY_PATH:}
      public-key-path:  ${IWM_PAYROLL_DOC_SIGNING_PUBLIC_KEY_PATH:}
```

**Two modes:**

| Mode | When | Behaviour |
|---|---|---|
| **Production** | both `*_KEY_PATH` env vars point at PEM files | the keys are loaded once at startup and reused for every document |
| **Development** | the paths are empty | a fresh in-memory RSA-2048 keypair is generated at boot, with a warning. **Documents signed before a restart will no longer verify after it** |

The same key signs all documents of the deployment. The `keyId` is stored on
each document so a future rotation (multiple active keys) can still pick the
right public key to verify older documents.

## Generating a production key

Generate a PKCS#8 private key and the matching X.509 public key with `openssl`:

```bash
# 1. RSA-2048 private key (PKCS#8 PEM, unencrypted — protect via filesystem permissions)
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 \
    -out /etc/iwm/payroll-doc-signing.private.pem

# 2. Matching public key (X.509 SubjectPublicKeyInfo PEM)
openssl rsa -in  /etc/iwm/payroll-doc-signing.private.pem \
            -pubout -out /etc/iwm/payroll-doc-signing.public.pem

# 3. Restrict access (owned by the service account)
chmod 600 /etc/iwm/payroll-doc-signing.private.pem
chmod 644 /etc/iwm/payroll-doc-signing.public.pem
```

Then point the service at them:

```bash
export IWM_PAYROLL_DOC_SIGNING_KEY_ID=payroll-doc-key-2026
export IWM_PAYROLL_DOC_SIGNING_PRIVATE_KEY_PATH=/etc/iwm/payroll-doc-signing.private.pem
export IWM_PAYROLL_DOC_SIGNING_PUBLIC_KEY_PATH=/etc/iwm/payroll-doc-signing.public.pem
```

## Key rotation

To rotate, generate a new key pair, point the env vars at the new files and
restart. Old documents keep their old `keyId` on the row, so a future
multi-key provider can verify them with the right public key. As long as you
keep the previous PEMs accessible, no document becomes unverifiable.

## Security notes

- The private key signs legally-opposable documents — treat it like a TLS
  private key (filesystem perms, secret store, no checkout in git).
- The public key may be distributed freely; it is what an auditor uses to
  re-verify a printed PDF (with its visible verification code + hash).
- The signature is *detached*: it does not embed in a PDF signature field
  (no PAdES). The PDF prints its hash and verification code so an external
  party can call `/{id}/verify` to confirm authenticity.
