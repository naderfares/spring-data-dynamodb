# Release Process

## Automated Deployment (Recommended)

### SNAPSHOT Deployment (Continuous)

SNAPSHOT versions are automatically deployed to Maven Central Portal when code is pushed to the `develop` branch:

1. Push changes to `develop` branch
2. GitHub Actions automatically runs tests and deploys SNAPSHOT
3. SNAPSHOT is available at: `https://central.sonatype.com/repository/maven-snapshots/`

**Workflow:** `.github/workflows/deploy.yml` (deploy-snapshot job)

### Release Deployment (Tagged Releases)

Release versions are deployed when a release tag is created:

1. **Prepare release locally:**
   ```bash
   # Format code and update licenses
   mvn formatter:format
   mvn license:format

   # Prepare and perform release
   mvn --batch-mode release:prepare
   mvn release:perform
   ```

2. **Push tags to GitHub:**
   ```bash
   git push origin --tags
   ```

3. **Automated deployment:**
    - GitHub Actions detects the new tag
    - Runs tests and deploys to Maven Central
    - Creates GitHub Release with artifacts
    - Maven Central sync takes ~15-30 minutes

**Workflow:** `.github/workflows/deploy.yml` (deploy-release job)

## Manual Deployment (Alternative)

If automated deployment fails or you need to deploy manually:

### Prerequisites

1. Configure Maven credentials in `~/.m2/settings.xml`:
   ```xml
   <servers>
     <server>
       <id>central</id>
       <username>${env.MAVEN_CENTRAL_USERNAME}</username>
       <password>${env.MAVEN_CENTRAL_TOKEN}</password>
     </server>
   </servers>
   ```

2. Configure GPG for signing artifacts

### Deploy SNAPSHOT

```bash
mvn clean deploy
```

### Deploy Release

```bash
# Prepare release (updates versions, creates tag)
mvn --batch-mode release:prepare

# Perform release (builds and deploys)
mvn release:perform
```

## Release Checklist

Before releasing:

1. ✅ Check `pom.xml` for the proper `<version />` tag (should end with `-SNAPSHOT`)
2. ✅ Update `src/changes/changes.xml` with release notes
3. ✅ Update `README.md` version examples
4. ✅ Run tests: `mvn clean verify`
5. ✅ Format code: `mvn formatter:format`
6. ✅ Update license headers: `mvn license:format`
7. ✅ Commit all changes to `develop`

After releasing:

1. ✅ Verify release on Maven Central: https://central.sonatype.com/artifact/io.github.naderfares/spring-data-dynamodb
2. ✅ Create new SNAPSHOT section in `src/changes/changes.xml`
3. ✅ Update documentation if needed

## GitHub Secrets Required

For automated deployment, configure these secrets in GitHub repository settings:

- `MAVEN_CENTRAL_USERNAME` - Maven Central Portal username
- `MAVEN_CENTRAL_TOKEN` - Maven Central Portal token
- `GPG_PRIVATE_KEY` - GPG private key for signing (export with `gpg --export-secret-keys --armor KEY_ID`)
- `GPG_PASSPHRASE` - GPG key passphrase

## Branch Strategy

- `develop` - Development branch, auto-deploys SNAPSHOT versions
- `main` - Production branch, used for release preparation
- Tags (`v*`) - Trigger release deployment to Maven Central
