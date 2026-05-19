| Case (Organisation) | Brief Description of Needs | Required Data | Verification Logic | Response / Outcome |
|---|---|---|---|---|
| Central Authority | Manage the full Digital ID lifecycle including creation, updates, restrictions, suspension, revocation, and expiry. | Actor role | Must be `CENTRAL_AUTHORITY` | Allow or reject management operation |
|  |  | Digital ID number | Must follow valid ID format and be unique on creation | Identity created or rejected |
|  |  | Date of birth | Immutable after creation | Reject immutable update attempts |
|  |  | Legal name | Must be valid and non-empty | Update allowed if valid |
|  |  | Current identity status | Must follow valid status transitions | Reject invalid transitions |
|  |  | Restriction dates | End date cannot precede start date | Restriction accepted or rejected |
| Employer | Verify whether a person currently has a valid Digital ID for employment/right-to-work purposes. | Digital ID number | Identity must exist | Valid or invalid result |
|  |  | Current status | Status must be `ACTIVE` | Right-to-work approved or rejected |
|  |  | Current timestamp | Verification evaluated against current time | Deterministic verification timestamp |
| Bank | Perform limited KYC verification using customer-submitted information. | Digital ID number | Identity must exist | KYC process continues or fails |
|  |  | Current status | Identity must be active | Verification allowed or blocked |
|  |  | Submitted legal name | Must match stored legal name | `nameMatched = true/false` |
|  |  | Submitted date of birth | Must match stored DOB | `dobMatched = true/false` |
|  |  | Financial review restriction | No blocking financial review restriction may exist | KYC accepted or flagged |
| Tax Authority | Verify whether an identity was valid during a reporting period. | Digital ID number | Identity must exist | Continue or reject verification |
|  |  | Reporting period start date | Must be before end date | Reject invalid period |
|  |  | Reporting period end date | Must be after start date | Deterministic evaluation |
|  |  | Status history | Identity must not be suspended during reporting period | Eligible or ineligible for return |
| Driving Licence Authority | Determine whether a person is eligible for licence issue or renewal. | Digital ID number | Identity must exist | Continue or reject |
|  |  | Current status | Identity must be active | Eligibility check proceeds |
|  |  | Date of birth | Must satisfy minimum age requirement | `minimumAgeMet = true/false` |
|  |  | Driving restrictions | No active `DRIVING_BAN` restriction | Eligible or blocked |
| Welfare Service | Check welfare eligibility using limited profile information. | Digital ID number | Identity must exist | Continue or reject |
|  |  | Current identity status | Identity must be active | Eligibility allowed or denied |
|  |  | Residential region | Must match requested region | `regionMatched = true/false` |
|  |  | Welfare band | Must satisfy welfare eligibility rules | Eligible or not eligible |
| Immigration Body | Determine whether an identity is permitted for immigration or border processing. | Digital ID number | Identity must exist | Continue or reject |
|  |  | Current status | Identity must be active | Entry processing allowed or denied |
|  |  | Nationality | Nationality information must exist | Nationality recognised or flagged |
|  |  | Border restrictions | No active `BORDER_HOLD` restriction | Entry allowed or blocked |
| Local Authority | Confirm whether a resident belongs to the authority’s region. | Digital ID number | Identity must exist | Continue or reject |
|  |  | Current identity status | Identity must be active | Residency verification proceeds |
|  |  | Local authority region | Must match stored residential region | Resident confirmed or denied |
| Education Provider | Verify educational eligibility using age band and residency. | Digital ID number | Identity must exist | Continue or reject |
|  |  | Current identity status | Identity must be active | Eligibility evaluation proceeds |
|  |  | Date of birth | Used internally to derive age band | Age band eligibility determined |
|  |  | Residential region | Must match educational region requirements | Region eligibility result |
| Healthcare Service | Verify regional healthcare access permissions. | Digital ID number | Identity must exist | Continue or reject |
|  |  | Current identity status | Identity must be active | Access allowed or denied |
|  |  | Residential region | Must match healthcare region | Regional access confirmed or denied |
|  |  | Emergency override category | May permit limited emergency-only access | Emergency access granted if applicable |
| Auditor | Review system behaviour and audit history without accessing full identity data. | Auditor role | Actor must be authorised auditor | Audit query allowed or rejected |
|  |  | Audit filters | Filters must be valid and authorised | Matching events returned |
|  |  | Audit event metadata | Only summaries may be viewed | Event summaries returned without sensitive data |