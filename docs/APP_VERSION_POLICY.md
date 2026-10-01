# App Version Policies

Policies are stored independently for Android and iOS in `app_version_policies`.
Hibernate creates or updates the table through the existing JPA schema setting.

## Public Flutter Endpoint

`GET /api/v1/app/version?platform=ANDROID`

The `platform` query parameter accepts `ANDROID` or `IOS`. An active policy returns
the following fields; disabled or missing policies return `404`.

```json
{
  "platform": "android",
  "latestVersion": "1.5.0",
  "minimumVersion": "1.4.0",
  "forceUpdate": false,
  "title": "UPDATE AVAILABLE",
  "message": "A new version of NROQ is available on Google Play.",
  "buttonText": "UPDATE NOW",
  "storeUrl": "https://play.google.com/store/apps/details?id=com.nroq.in"
}
```

The Flutter app compares the installed version against the policy. Versions below
`minimumVersion` require an update; versions from `minimumVersion` up to but not
including `latestVersion` may continue; `forceUpdate: true` makes all outdated
versions require an update. Versions at or above `latestVersion` continue normally.

## Admin Endpoints

All `/api/v1/admin/app-versions` endpoints require an authenticated `ADMIN` role.

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/v1/admin/app-versions` | Create a platform policy |
| `GET` | `/api/v1/admin/app-versions` | List Android and iOS policies |
| `GET` | `/api/v1/admin/app-versions/{platform}` | Read one policy |
| `PUT` | `/api/v1/admin/app-versions/{platform}` | Replace one policy |
| `PATCH` | `/api/v1/admin/app-versions/{platform}/status` | Set `{ "enabled": true }` or `false` |

Create and update requests use these fields:

```json
{
  "platform": "ANDROID",
  "latestVersion": "1.5.0",
  "minimumVersion": "1.4.0",
  "forceUpdate": false,
  "title": "UPDATE AVAILABLE",
  "message": "A new version of NROQ is available.",
  "buttonText": "UPDATE NOW",
  "storeUrl": "https://play.google.com/store/apps/details?id=com.nroq.in"
}
```

Versions must use `major.minor.patch` format, and `minimumVersion` cannot exceed
`latestVersion`. Only one policy can exist per platform; use `PUT` to change it.