# SugarCane Invoice Manager

Android app for managing sugar-cane invoices, cancellation requests and admin approvals.

## Features
- Dashboard with invoice/request counts
- Search invoices by invoice number or farmer
- West Kenya and Mumias/Butali company filters
- Cancellation request workflow
- Admin approval/rejection
- Active, pending, approved and cancelled statuses
- Audit-friendly: invoices are not silently deleted
- GitHub Actions workflow for automatic debug APK builds

## Build on GitHub
1. Upload the contents of this project to the root of your GitHub repository.
2. Open **Actions**.
3. Select **Build Android APK**.
4. Run the workflow or push to `main`.
5. Download the `sugarcane-invoice-manager-debug` artifact from the completed workflow.

## Local build
Requires JDK 17 and Gradle 8.10.2 (or a compatible Gradle installation).

```bash
gradle assembleDebug
```

The APK is generated at:
`app/build/outputs/apk/debug/app-debug.apk`
