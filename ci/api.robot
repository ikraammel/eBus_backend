*** Settings ***
Documentation    Existing accounts on the CI backend image connected to the remote database. Reads and login only.
Library    api.py
Suite Setup    Connect Existing User
Suite Teardown    Close Existing Session
Test Template    Check Existing Account
Test Tags    readonly    existing-accounts

*** Test Cases ***
CI-API-001 Health                    health
CI-API-002 Bus                       bus
CI-API-003 Lines                     lines
CI-API-004 Tickets                   tickets
CI-API-005 Anonymous Types           anonymous-types
CI-API-006 Own Profile               profile
CI-API-007 Own Dossier               dossier
CI-API-008 Own Subscription          subscription
CI-API-009 Own History               history
CI-API-010 Own Cards                 cards
CI-API-011 Own Activities            activities
CI-API-012 Authenticated Types       authenticated-types
