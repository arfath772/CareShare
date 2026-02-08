# Security Setup Guide

## Environment Variables Configuration

This application uses environment variables to protect sensitive credentials.

### Required Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_PASSWORD` | MySQL database password | `your_db_password` |
| `DB_USERNAME` | MySQL username (optional, defaults to root) | `root` |
| `DB_URL` | Database URL (optional, defaults to localhost) | `jdbc:mysql://localhost:3306/zero_db` |
| `JWT_SECRET` | JWT signing key (256+ bits) | `your_random_secret_key` |
| `JWT_EXPIRATION` | Token expiration time in ms (optional) | `86400000` |
| `MAIL_USERNAME` | Gmail account for sending emails | `youremail@gmail.com` |
| `MAIL_PASSWORD` | Gmail app password | `your_app_password` |

---

## Setup Instructions

### Option 1: Windows PowerShell (Current Session)
```powershell
$env:DB_PASSWORD="9113611658"
$env:JWT_SECRET="531be2ab7565b40e10b71ed2cb7d64382ac3087a2fdff7b67d87a1e9e91921de128c5db71713bffd83f030c4cb9afdb8"
$env:MAIL_PASSWORD="mxlxlcwxugrizfmh"
```

Then run: `.\mvnw.cmd spring-boot:run`

### Option 2: Windows System Environment Variables (Permanent)
1. Search "Environment Variables" in Windows
2. Click "Environment Variables" button
3. Under "User variables" click "New"
4. Add each variable name and value
5. Restart your terminal/IDE

### Option 3: IntelliJ IDEA / VS Code
**IntelliJ IDEA:**
1. Go to Run → Edit Configurations
2. Add environment variables in "Environment Variables" field
3. Format: `DB_PASSWORD=yourpass;JWT_SECRET=yoursecret;MAIL_PASSWORD=yourmail`

**VS Code:**
1. Create `.env` file (add to .gitignore)
2. Install "Spring Boot Extension Pack"
3. Variables will be loaded automatically

### Option 4: Maven Command Line
```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--spring.datasource.password=yourpass --spring.mail.password=yourmail --jwt.secret=yoursecret"
```

---

## Important Security Notes

✅ **NEVER commit these to Git:**
- `.env` files with real credentials
- `application.properties` with hardcoded passwords

✅ **For Production:**
- Use Azure Key Vault, AWS Secrets Manager, or HashiCorp Vault
- Use different credentials for dev/staging/prod
- Rotate secrets regularly

✅ **Git Safety:**
Add to `.gitignore`:
```
.env
application-local.properties
```

✅ **If you already committed passwords:**
1. Change all passwords immediately
2. Remove from Git history: `git filter-branch` or BFG Repo-Cleaner
3. Force push to remove from remote

---

## Generating Secure JWT Secret

```powershell
# Generate a secure 512-bit key
[Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(64))
```
