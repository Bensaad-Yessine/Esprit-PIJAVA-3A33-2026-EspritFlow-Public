# Email Templates - EspritFlow

## Overview
The application now sends beautifully designed HTML emails for password reset, user bans, and user unbans.

## Email Templates

### 1. Password Reset Email
**File**: `PasswordResetService.java`
**Sent when**: User requests a password reset via "Forgot password?" screen
**Design**: Red/White theme (matches login/reset-password screens)
**Contents**:
- Lock icon header ([SECURITE])
- Welcome message with user first name
- Reset token displayed in a styled code box
- Expiry time warning (30 minutes)
- Step-by-step instructions for password reset
- Security notice about not sharing the code
- Support contact information

### 2. Ban Notification Email
**File**: `BanNotificationService.java` (new)
**Sent when**: Administrator bans a user
**Design**: Red gradient header with warning styling
**Contents**:
- Warning header ([ALERTE])
- Explanation that account is suspended
- Reason for suspension in a highlighted box
- List of consequences
- Instructions on how to contact support to appeal
- Support email and timestamp

### 3. Unban Notification Email
**File**: `UserServices.java` (unbanUser method)
**Sent when**: Administrator unbans a user
**Design**: Green gradient header with success styling
**Contents**:
- Success header ([SUCCES])
- Confirmation that account is reactivated
- List of what user can do again
- Call-to-action button to login
- Support contact information

## Technical Details

### Encoding
All emails use UTF-8 encoding. Special characters and accents are properly handled:
- Emojis are replaced with `[TAG]` format for compatibility (e.g., `[SECURITE]`, `[ALERTE]`)
- Accented characters are preserved in HTML (e.g., "Réinitialisation", "Bienvenue")

### Styling
- **Red theme**: `#dc2626` (ban/reset), `#b91c1c` (darker red)
- **Green theme**: `#16a34a` (unban success), `#15803d` (darker green)
- **Typography**: Arial font, responsive design
- **Colors**:
  - Background: `#f5f5f5`
  - Card: `#ffffff`
  - Text: `#333333`
  - Accent: `#dc2626` or `#16a34a`

### Structure
All emails follow this HTML structure:
```html
<body>
  <email-container>
    <header> <!-- Gradient background, red or green -->
    <content> <!-- Main message with action items -->
    <footer> <!-- Copyright and email address -->
  </email-container>
</body>
```

## Configuration Required

Make sure `.env` contains:
```
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your-email@gmail.com
SMTP_PASSWORD=your-app-password
SMTP_FROM=your-email@gmail.com
SMTP_STARTTLS=true
SMTP_SSL=false
```

## Integration Points

### In Controllers/Services:
1. **PasswordResetService**: Automatically called when user requests password reset
2. **UserServices.banUser()**: Automatically sends ban notification
3. **UserServices.unbanUser()**: Automatically sends unban notification

### Example Usage:
```java
// Ban a user and send notification
userServices.banUser(userId, "Spam and harassment");

// Unban a user and send notification
userServices.unbanUser(userId);
```

## Testing

To test emails locally:
1. Configure real SMTP credentials in `.env`
2. Create a test user
3. Call the corresponding method:
   ```java
   // Test password reset
   PasswordResetService service = new PasswordResetService();
   service.requestReset("test@example.com");
   
   // Test ban notification
   UserServices userService = new UserServices();
   userService.banUser(1, "Test ban reason");
   
   // Test unban notification
   userService.unbanUser(1);
   ```

## Troubleshooting

### Email not sending?
- Check SMTP credentials in `.env`
- Verify SMTP host/port are correct
- Enable "Less secure app access" if using Gmail
- Generate app-specific password for Gmail (recommended)
- Check firewall/network access to SMTP port

### Special characters appearing wrong?
- All files use UTF-8 encoding
- Make sure your IDE/terminal uses UTF-8
- Email clients should render HTML correctly

### HTML not rendering?
- Most email clients support inline CSS
- Test with major clients (Gmail, Outlook, Apple Mail)
- Fallback text is provided for clients that don't support HTML


