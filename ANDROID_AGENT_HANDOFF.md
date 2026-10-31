# LungCare AI — Android Studio Agent Handoff

انسخ قسم **MASTER IMPLEMENTATION PROMPT** بالكامل إلى الـAgent داخل Android Studio، وأرفق معه صور الشاشات أو رابط المعاينة ومجلد `src` من المشروع الحالي.

## Recommended Android Stack

- Kotlin
- Jetpack Compose + Material 3
- Navigation Compose
- ViewModel + StateFlow
- Repository pattern
- DataStore for language, theme, onboarding state, and prototype preferences
- Room for local prototype records, symptoms, appointments, and saved questions
- CameraX for X-ray capture
- Android Photo Picker for existing images
- Coil for local image preview
- `stringResource` with `values/strings.xml` and `values-ar/strings.xml`
- AppCompat per-app language APIs
- Edge-to-edge layouts
- Dynamic color disabled by default so the LungCare design system remains consistent
- Retrofit + OkHttp for the backend API
- Kotlin Serialization for request and response models
- Encrypted local token storage

---

# MASTER IMPLEMENTATION PROMPT

You are a senior Android engineer and healthcare mobile UX implementation specialist.

Build a production-quality Android application named **LungCare AI** using **Kotlin and Jetpack Compose**.

The attached web prototype and screenshots are the visual source of truth. Reproduce its information architecture, visual hierarchy, spacing, component styling, light/dark themes, Arabic/English localization, and navigation behavior. Do not redesign it into a generic Material app.

## 1. Product purpose

LungCare AI supports two equally important journeys:

1. Early detection education:
   - Risk-factor organization
   - Screening education
   - Symptom monitoring
   - Preparing questions for clinicians

2. Diagnosed-patient care organization:
   - Clinician-confirmed diagnosis information
   - Treatment information
   - Side-effect tracking
   - Medication reminders
   - Appointments and follow-up

The application is not an autonomous cancer diagnosis system.

## 2. Required Android architecture

Create a single-activity Compose application.

Use:

- `MainActivity`
- `NavHost`
- Feature-based packages
- ViewModels exposing immutable `UiState`
- Repository interfaces
- Local prototype repositories initially
- Dependency injection with Hilt
- Room for user-created prototype data
- DataStore for theme and language preferences

Suggested package structure:

```text
com.lungcare.ai
├── app
├── core
│   ├── design
│   ├── model
│   ├── navigation
│   ├── database
│   ├── localization
│   └── ui
├── feature
│   ├── onboarding
│   ├── home
│   ├── risk
│   ├── symptoms
│   ├── assistant
│   ├── records
│   ├── vision
│   ├── care
│   ├── education
│   ├── notifications
│   └── profile
└── data
```

Do not put all screens in `MainActivity` or one Kotlin file.

## 2.1 Backend is required

This application will connect to a real backend and a separately developed AI model.

Do not:

- Keep important health records only in Compose state.
- Send user answers directly from the Android app to the AI model.
- embed an AI model secret, database password, or private API key in the APK.
- Let the Android app connect directly to PostgreSQL or object storage.

Required data flow:

```text
Android App
    │
    │ HTTPS + authenticated API
    ▼
Backend API
    ├── Authentication and authorization
    ├── Validation and consent checks
    ├── PostgreSQL database
    ├── Private object storage for X-ray files
    ├── Audit trail
    └── AI orchestration service
             │
             ▼
       Lung AI Model Service
```

The Android app communicates only with the Backend API. The backend is responsible for communicating with the AI model.

Recommended backend stack:

- Python 3.12
- FastAPI
- PostgreSQL
- SQLAlchemy 2 + Alembic
- Pydantic
- Private S3-compatible object storage
- Redis plus a task queue for long-running image analysis
- OAuth2/JWT access tokens with refresh-token rotation
- Docker for local and production deployment

The AI model may run:

- As a separate internal FastAPI model service
- In a private container
- On an approved private inference provider

The model service must not be publicly callable from the Android application.

## 2.2 Where user answers are stored

User information must be separated by purpose:

| Information | Destination | Notes |
|---|---|---|
| Profile and preferences | PostgreSQL | Language, theme, journey selection |
| Consent choices | PostgreSQL consent table | Versioned and timestamped |
| Risk questionnaire answers | PostgreSQL | Patient-entered, editable |
| Smoking history | PostgreSQL | Patient-entered, optional |
| Symptoms and side effects | PostgreSQL | Patient-entered timeline |
| Doctor questions | PostgreSQL | Private user content |
| Appointments | PostgreSQL | Patient-entered unless imported |
| Diagnosis and treatment details | PostgreSQL | Store source and confirmation status |
| X-ray or medical document files | Private object storage | Never store large files directly in the database |
| File metadata | PostgreSQL | Owner, type, status, dates, storage key |
| AI analysis job | PostgreSQL | Status, model version, timestamps |
| AI output | PostgreSQL | Clearly labeled AI-generated; never clinician-confirmed |

Every health-data record must include:

```text
id
userId
sourceType
verificationStatus
createdAt
updatedAt
```

Suggested source types:

```text
PATIENT_ENTERED
CLINICIAN_PROVIDED
IMPORTED
AI_GENERATED
```

Suggested verification states:

```text
UNVERIFIED
USER_CONFIRMED
CLINICIAN_CONFIRMED
```

Never automatically change an AI-generated result into clinician-confirmed data.

## 2.3 Offline and synchronization behavior

Use Room as an offline cache, not the only source of truth.

Required behavior:

- Save unfinished questionnaire drafts locally.
- Synchronize completed records with the backend after authentication.
- Queue offline changes and retry when connectivity returns.
- Show sync status when relevant.
- Resolve conflicts using backend version numbers and `updatedAt`.
- Do not cache raw X-ray images longer than necessary.
- Provide a local-delete operation after a successful secure upload.
- Do not display a successful upload until the backend confirms it.

## 2.4 Backend API contract

Use versioned endpoints under:

```text
/api/v1
```

Minimum endpoints:

```text
POST   /auth/register
POST   /auth/login
POST   /auth/refresh
POST   /auth/logout

GET    /me
PATCH  /me
GET    /me/preferences
PATCH  /me/preferences

GET    /consents
POST   /consents

POST   /risk-assessments
GET    /risk-assessments
GET    /risk-assessments/{id}
PATCH  /risk-assessments/{id}

GET    /symptoms
POST   /symptoms
PATCH  /symptoms/{id}
DELETE /symptoms/{id}

GET    /side-effects
POST   /side-effects
PATCH  /side-effects/{id}
DELETE /side-effects/{id}

GET    /medical-records
POST   /medical-records
GET    /medical-records/{id}
PATCH  /medical-records/{id}
DELETE /medical-records/{id}

POST   /uploads/presign
POST   /uploads/{id}/complete

POST   /ai/xray/jobs
GET    /ai/xray/jobs/{jobId}
DELETE /ai/xray/jobs/{jobId}

POST   /ai/chat/messages
GET    /ai/chat/conversations
GET    /ai/chat/conversations/{id}

GET    /appointments
POST   /appointments
PATCH  /appointments/{id}
DELETE /appointments/{id}

GET    /doctor-questions
POST   /doctor-questions
PATCH  /doctor-questions/{id}
DELETE /doctor-questions/{id}

POST   /account/export
DELETE /account
```

Generate OpenAPI documentation from the backend and use it as the contract between Android and backend teams.

## 2.5 X-ray AI request flow

Implement the following flow:

1. Android requests a secure upload URL from `/uploads/presign`.
2. Backend verifies authentication, consent, file type, and file-size limits.
3. Android uploads the encrypted file using the short-lived URL.
4. Android confirms completion with `/uploads/{id}/complete`.
5. Android creates an AI job using `/ai/xray/jobs`.
6. Backend queues the job.
7. An internal worker sends the image to the private AI model service.
8. The model returns a structured response to the backend.
9. Backend validates and stores the result with the model version.
10. Android polls `/ai/xray/jobs/{jobId}` or receives an approved push event.
11. Android displays the result as AI-generated support, not a confirmed diagnosis.

Job statuses:

```text
PENDING_UPLOAD
QUEUED
PROCESSING
COMPLETED
FAILED
REQUIRES_NEW_IMAGE
CANCELLED
```

The AI response contract should be versioned:

```json
{
  "jobId": "uuid",
  "status": "COMPLETED",
  "model": {
    "name": "internal-model-name",
    "version": "1.0.0"
  },
  "quality": {
    "acceptable": true,
    "issues": []
  },
  "educationalOutput": {
    "summary": "AI-generated educational output",
    "limitations": ["Not a diagnosis", "Requires clinician review"]
  },
  "createdAt": "ISO-8601 timestamp"
}
```

Do not return unsupported cancer probabilities or a definitive positive/negative diagnosis unless the model, intended use, clinical validation, regulatory pathway, and supervising medical workflow explicitly support it.

## 2.6 Authentication and access control

Required:

- Short-lived access tokens.
- Refresh-token rotation.
- Server-side token revocation.
- Ownership checks on every health record.
- Users can access only their own records.
- Admin access must use a separate role and interface.
- AI workers receive only the minimum data needed for inference.
- Signed object-storage URLs must expire quickly.
- Never expose permanent file URLs.

The Android app must handle:

- Signed out
- Access token expired
- Refresh successful
- Refresh failed
- Account disabled
- Network unavailable
- Server validation error
- Rate limited

## 2.7 Privacy, security, and audit requirements

- Use HTTPS only.
- Encrypt database volumes and object storage at rest.
- Never include health data, tokens, report contents, or X-ray URLs in application logs.
- Validate file MIME type and content.
- Apply file-size limits.
- Scan uploads before model processing.
- Store consent version and timestamp.
- Record security-relevant actions in an audit log.
- Provide account export and deletion workflows.
- Define retention and deletion policies before production use.
- Keep production, staging, and development data separated.
- Use synthetic data in development.
- Do not claim HIPAA, GDPR, or other regulatory compliance until formally reviewed and implemented.

## 2.8 Backend environments

Use separate configurations:

```text
Development
Staging
Production
```

Android must read only a public backend base URL from build configuration:

```text
API_BASE_URL
```

Secrets remain on the backend. Do not store them in:

- `strings.xml`
- `BuildConfig`
- Git
- the APK
- local Android resources

## 2.9 Android networking boundary

Create interfaces such as:

```kotlin
interface RiskAssessmentRepository
interface SymptomRepository
interface MedicalRecordRepository
interface AppointmentRepository
interface AiAssistantRepository
interface XrayAnalysisRepository
```

Provide:

- `Remote...Repository` implementations using the API
- `Fake...Repository` implementations for previews and tests
- Room-backed cache where appropriate

Do not call Retrofit directly from Composables or ViewModels.

## 3. Design system

Implement a dedicated Compose design system.

### Light theme

```text
Primary:        #176B67
Secondary:      #2F8F89
Soft mint:      #EAF6F3
Background:     #F7F9FB
Surface:        #FFFFFF
Primary text:   #1D2939
Secondary text: #667085
Border:         #E4E7EC
Info:           #3478C5
Warning:        #D99022
Urgent:         #C83E4D
Success:        #27845B
```

### Dark theme

```text
Background:     #081011
Surface:        #111A1B
Elevated:       #172526
Primary text:   #F3F7F6
Secondary text: #9BADAA
Border:         #263334
Primary:        #56C5B3
Secondary:      #73D2C2
Soft mint:      #142B29
```

### Shape and spacing

- 8dp base spacing system
- Main horizontal screen padding: 20dp
- Cards: 16–20dp radius
- Inputs: 12–14dp radius
- Buttons: 14dp radius
- Main touch targets: at least 48dp
- Restrained elevation; use borders and tonal surfaces more than heavy shadows
- Mobile reference viewport: 390 × 844

### Typography

- English: Inter or system sans equivalent
- Arabic: Noto Sans Arabic
- Large display: 28–30sp, bold
- Page title: 20sp, bold
- Section title: 15–17sp, bold
- Body: 14sp
- Supporting text: 11–12sp
- Captions: minimum 10sp where possible

Use proper Arabic line height and avoid clipping Arabic glyphs.

### Reusable Compose components

Create:

- `LungCareButton`
- `LungCareIconButton`
- `LungCareCard`
- `LungCareTopBar`
- `LungCareBottomBar`
- `LungCareTextField`
- `LungCareNotice`
- `LungCareTag`
- `LungCareListRow`
- `JourneySelectionCard`
- `MedicalRecordCard`
- `AppointmentCard`
- `TimelineItem`
- `SymptomSeveritySelector`
- `AssistantMessageBubble`
- `EmptyState`
- `LoadingState`
- `ErrorState`
- `ConsentPanel`
- `XrayCaptureCard`

## 4. Localization requirements

The entire application must support:

- English
- Arabic

Requirements:

- Every visible string must come from Android string resources.
- Use `values/strings.xml` and `values-ar/strings.xml`.
- Never hardcode UI text in composables.
- Switching language must work from Welcome, Home, and Profile.
- Persist the selected language using DataStore.
- Arabic must use full RTL layout.
- Directional icons must mirror correctly.
- Medical abbreviations such as LDCT can remain Latin with an Arabic explanation.
- User-entered data must not be translated.

## 5. Theme requirements

- Full light and dark theme support.
- Theme switch available on Welcome, Home, and Profile.
- Persist the selected theme.
- All cards, forms, dialogs, charts, timelines, notices, and navigation components must support both themes.
- Do not implement dark mode as simple color inversion.

## 6. Primary navigation

Persistent bottom navigation:

1. Home / الرئيسية
2. My Health / صحتي
3. AI Assistant / المساعد
4. Care Journey / رحلتي
5. Profile / حسابي

Use typed Navigation Compose routes.

Hide bottom navigation on:

- Onboarding
- Full-screen forms
- Risk-assessment steps
- Record details
- X-ray capture
- Privacy
- Design-system preview

## 7. Required screen inventory

Implement these screens:

### Onboarding

1. Splash
2. Welcome
3. Choose journey
4. Create account
5. Sign in
6. Basic profile setup
7. Health context and consent

### Home

8. Personalized home
9. Notification center

### Risk and screening

10. Risk introduction
11. Personal background
12. Smoking history
13. Additional factors
14. Symptoms and concerns
15. Assessment review
16. Risk Factor Summary
17. Screening and LDCT education

### Symptoms

18. Symptom overview
19. Add symptom
20. Symptom history
21. Urgent-symptom safety state

### AI Assistant

22. Assistant home
23. Conversation
24. Loading, retry, copy, save, and feedback states

### Medical records

25. Records overview
26. Add record
27. Imaging information
28. Record details

### Computer Vision prototype

29. X-ray capture and upload
30. Image-quality checking state
31. Image-ready-for-clinician-review state

### Patient care

32. Care journey
33. Diagnosis information
34. Treatment overview
35. Treatment schedule
36. Side-effect tracker
37. Add side effect
38. Medication list and reminders
39. Appointment management
40. Follow-up timeline

### Education and support

41. Education hub
42. Article details
43. Questions for doctor
44. Emotional and practical support

### Profile

45. Profile and preferences
46. Privacy and consent
47. Design-system preview

## 8. Journey-aware home screen

Home must adapt to:

- Early detection
- Under medical evaluation
- Diagnosed patient

Header:

- Greeting
- User avatar
- Language switch
- Theme switch
- Notifications

Primary hero card changes based on journey.

Quick actions:

- Track symptom
- Screening guide
- Questions for doctor
- Medical records

Add a prominent dark **Computer Vision Lab** card:

- Animated scanning line
- X-ray/lung visual
- “Capture or upload a chest X-ray”
- “Demo” label
- Opens the X-ray capture screen

## 9. Computer Vision and camera

Use CameraX for capture and Android Photo Picker for upload.

Required states:

1. Empty capture frame
2. Camera permission rationale
3. Permission denied state
4. Captured image preview
5. Upload preview
6. Image-quality loading state
7. Quality-check completed state
8. Unsupported file/error state

For the initial prototype:

- Keep the image locally.
- Do not upload it to a server.
- Do not create a cancer result.
- Do not classify images as cancer-positive or cancer-negative.
- The “quality check” may only confirm prototype display quality.
- Clearly label that real analysis requires a validated medical system and specialist oversight.

Abstract the future model behind:

```kotlin
interface XrayAnalysisRepository {
    suspend fun checkImageQuality(uri: Uri): ImageQualityResult
}
```

Use a fake implementation initially.

## 10. Data models

Create models for:

```kotlin
UserProfile
JourneyType
RiskAssessment
SmokingHistory
RiskFactorSummary
SymptomEntry
SideEffectEntry
MedicalRecord
ImagingRecord
DiagnosisInfo
TreatmentInfo
Medication
Appointment
FollowUpMilestone
DoctorQuestion
EducationArticle
ChatMessage
NotificationItem
```

Every clinician-confirmed field must be distinguishable from patient-entered information.

## 11. Prototype behavior

Use realistic fictional data.

User:

```text
Name: Moaz Ragab
Email: moazragab652@gamil.com
Initials: MR
Preferred name: Moaz
```

Support:

- Adding/editing/deleting symptoms
- Adding/editing/deleting local medical records
- Adding doctor questions
- Marking questions answered
- Theme switching
- Language switching
- Journey switching
- Appointment completion
- Save article
- Image selection and local preview

Use Room or in-memory fake repositories where appropriate. Authentication, AI, notifications, and medical integrations must remain clearly simulated unless a real backend is later supplied.

## 12. Medical safety rules

Apply these throughout:

- Never diagnose or exclude lung cancer.
- Never create unsupported cancer probabilities.
- Distinguish screening from diagnosis.
- Screening is generally for eligible people without symptoms.
- Concerning symptoms require clinical evaluation.
- A chest X-ray is not an equivalent replacement for recommended LDCT screening.
- Never invent stage, pathology, medication dosage, treatment, or imaging findings.
- Coughing up blood needs prompt medical assessment.
- Severe breathing difficulty or severe chest pain requires emergency guidance.
- Never advise users to start, stop, or change prescription medication independently.
- AI content is educational, not personalized medical advice.

## 13. Accessibility

- Minimum 48dp touch targets.
- TalkBack labels for all interactive controls.
- Do not rely on color alone.
- Support font scaling to at least 200%.
- Maintain contrast in light and dark modes.
- Announce validation errors.
- Arabic and English semantics must be correct.
- Camera instructions must be available as text, not only graphics.

## 14. Tests

Add:

- ViewModel unit tests
- Navigation tests for the primary journeys
- Compose UI tests for language switching
- Compose UI tests for theme switching
- RTL screenshot tests for key screens
- Camera permission-state tests
- Risk-assessment state restoration tests
- Room DAO tests

## 15. Implementation order

1. Project foundation and design tokens
2. Localization and theme infrastructure
3. Shared components
4. Navigation
5. Welcome and Home
6. Risk assessment
7. Symptoms
8. Records
9. Computer Vision prototype
10. Patient care
11. Education and AI
12. Profile and privacy
13. Tests and accessibility review

After each phase, compile the project and fix all errors before continuing.

## 16. Acceptance criteria

- The app builds successfully in Android Studio.
- No visible string is hardcoded.
- Every screen works in Arabic and English.
- Every screen supports light and dark mode.
- Arabic uses full RTL.
- Navigation is connected.
- Camera capture and Photo Picker work locally.
- Computer Vision does not produce unsupported medical findings.
- Core local add/edit flows work.
- The UI visually matches the supplied LungCare AI prototype.
- No primary action appears functional without a corresponding interaction.

Do not stop after creating the dashboard. Implement the complete screen structure and reusable design system.

---

## Files to give the Android Agent

Provide:

1. `ANDROID_AGENT_HANDOFF.md`
2. Screenshots of the main screens in light English, dark English, light Arabic, and dark Arabic
3. Current web source:
   - `src/App.tsx`
   - `src/index.css`
   - `src/i18n.ts`
4. Any exported logo or vector assets

## Recommended screenshot set

At minimum export:

- Welcome
- Choose Journey
- Home
- Home dark mode
- Home Arabic RTL
- Risk Assessment
- Risk Factor Summary
- Symptom Tracker
- AI Assistant
- Medical Records
- X-ray Vision Lab
- Care Journey
- Treatment Overview
- Appointment Details
- Education Hub
- Profile
- Privacy