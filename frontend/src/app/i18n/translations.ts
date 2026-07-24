import { AppLanguage } from "../core/models/language-settings.model";

export const TRANSLATIONS: Record<AppLanguage, Record<string, string>> = {
  en: {
    // ── Navigation ────────────────────────────────────────────────
    "nav.dashboard": "Dashboard",
    "nav.backlog": "Backlog",
    "nav.metrics": "Metrics",
    "nav.capacity": "Capacity",
    "nav.settings": "Settings",
    "sidebar.no_org": "No Organization",
    "sidebar.logout": "Logout",

    // ── Backlog page ──────────────────────────────────────────────
    "backlog.title": "Sprint Backlog",
    "backlog.subtitle": "Track and manage sprint issues",

    // ── Metrics page ──────────────────────────────────────────────
    "metrics.title": "Metrics & Analytics",
    "metrics.subtitle": "Sprint velocity, capacity, and historical comparisons",
    "metrics.view.all": "All",
    "metrics.view.velocity": "Velocity",
    "metrics.view.capacity": "Capacity",
    "metrics.view.topics": "Topics",

    // ── Capacity page ─────────────────────────────────────────────
    "capacity.title": "Capacity Planning",
    "capacity.subtitle": "Manage team availability and plan sprint capacity",

    // ── Sprint Dashboard ──────────────────────────────────────────
    "dashboard.brand_subtitle": "Sprint intelligence workspace",
    "dashboard.hero_kicker": "Agile reporting automation",
    "dashboard.current_sprint": "Current sprint",
    "dashboard.tab.board": "Board",
    "dashboard.tab.metrics": "Metrics",
    "dashboard.tab.capacity": "Capacity",
    "dashboard.read_only": "Read only",
    "dashboard.retry": "Retry",

    // ── Sprint Summary Card ───────────────────────────────────────
    "summary.total_issues": "Total Issues",
    "summary.total_sp": "Total SP",
    "summary.done_sp": "Done SP",
    "summary.remaining_sp": "Remaining SP",
    "summary.completion": "Completion",

    // ── Sprint Issue Table ────────────────────────────────────────
    "table.title": "Sprint Issues",
    "table.key": "Key",
    "table.summary": "Summary",
    "table.assignee": "Assignee",
    "table.type": "Type",
    "table.status": "Status",
    "table.total_sp": "Total SP",
    "table.remaining_sp": "Remaining SP",
    "table.done_sp": "Done SP",
    "table.action": "Action",
    "table.empty": "No issues found for this sprint.",
    "table.unassigned": "Unassigned",

    // ── Sprint Analytics ──────────────────────────────────────────
    "analytics.velocity.title": "Velocity",
    "analytics.velocity.subtitle":
      "Committed · Delivered · Work · Leftover · Ratio",
    "analytics.capacity.title": "Capacity & Team Availability",
    "analytics.capacity.subtitle": "Planned vs Real · EFT by role",
    "analytics.topics.title": "Story Points by Topic",
    "analytics.topics.subtitle": "Distribution across epics / topics",
    "analytics.iterations.title": "Iteration Comparison",
    "analytics.iterations.sprint_col": "Sprint",
    "analytics.iterations.delivered_col": "Delivered / Committed",
    "analytics.iterations.velocity_col": "Velocity",
    "analytics.iterations.ratio_col": "Ratio",
    "analytics.iterations.empty": "No iteration history available",
    "analytics.ratio_label": "Delivered / Committed Ratio",
    "analytics.planned": "Planned",
    "analytics.real": "Real",
    "analytics.delta": "Delta",
    "analytics.loading": "Loading analytics…",
    "analytics.team_availability": "Team Availability (EFT)",

    // ── Settings ──────────────────────────────────────────────────
    "settings.title": "Settings",
    "settings.subtitle": "Manage your organization configuration",
    "settings.tab.jira": "Jira Configuration",
    "settings.tab.billing": "Billing & Subscription",
    "settings.tab.language": "Language",
    "settings.tab.members": "Members",

    // ── Language settings ─────────────────────────────────────────
    "lang.title": "Language",
    "lang.subtitle": "Choose your preferred application language.",
    "lang.label": "Interface language",
    "lang.save": "Save language",
    "lang.saving": "Saving...",
    "lang.saved": "Language preference saved.",
    "lang.error": "Failed to save language preference.",
    "lang.loading": "Loading language preference...",

    // ── Members ───────────────────────────────────────────────────
    "members.title": "Members",
    "members.desc": "Manage your team members and pending invitations.",
    "members.filter.all": "All",
    "members.active.title": "Active Members",
    "members.active.empty": "No active members found.",
    "members.pending.title": "Pending Invitations",
    "members.pending.invited_on": "Invited",
    "members.status.verified": "Verified",
    "members.status.pending": "Pending",
    "members.invite.title": "Invite a Member",
    "members.invite.placeholder": "colleague@company.com",
    "members.invite.sending": "Sending…",
    "members.invite.send": "Invite",
    "members.invite.error_required": "Email is required.",
    "members.invite.error_invalid": "Please enter a valid email address.",
    "members.bulk.title": "Bulk Invite",
    "members.bulk.hint":
      "Paste emails separated by commas, semicolons or newlines.",
    "members.bulk.sending": "Sending…",
    "members.bulk.default_badge": "default",
    "members.bulk.dashboards_optional": "Dashboards (optional)",
    "members.bulk.dashboards_selected": "selected",
    "members.bulk.result_title": "Invitations sent",
    "members.bulk.invited": "Invited",
    "members.bulk.already_pending": "Already pending",
    "members.bulk.already_member": "Already member",
    "members.bulk.invalid": "Invalid",
    "members.limit.upgrade": "Upgrade your plan",

    // ── Jira Config ───────────────────────────────────────────────
    "jira.title": "Jira Configuration",
    "jira.credentials.title": "Credentials",
    "jira.credentials.connected": "Connected",
    "jira.credentials.modify": "Modify",
    "jira.credentials.cancel": "Cancel",
    "jira.credentials.save_test": "Save & Test Credentials",
    "jira.credentials.saving": "Saving & Testing…",
    "jira.credentials.locked_hint":
      "Configure and test your Jira credentials first.",
    "jira.dashboards.title": "Dashboards",
    "jira.dashboards.subtitle":
      "The active dashboard is used by all users to load sprint issues. The first dashboard created becomes active automatically.",
    "jira.dashboards.empty": "No dashboard configured. Add one below.",
    "jira.dashboards.active": "Active",
    "jira.dashboards.activate": "Activate",
    "jira.dashboards.add_title": "Add a dashboard",
    "jira.dashboards.create": "Create dashboard",
    "jira.dashboards.creating": "Creating…",
    "jira.dashboards.field_name": "Name",
    "jira.dashboards.delete": "Delete dashboard",

    // ── My Jira Credentials (personal) ─────────────────────────────────────────────
    "settings.tab.my_jira": "My Jira Account",
    "my_jira.title": "My Jira Account",
    "my_jira.subtitle":
      "Connect your personal Jira account to access sprint data.",
    "my_jira.field.username": "Jira username (email or login)",
    "my_jira.field.password": "Password or Personal Access Token",
    "my_jira.save": "Connect my Jira account",
    "my_jira.saving": "Connecting…",
    "my_jira.success": "Jira account connected. Redirecting to dashboard…",
    "my_jira.connected_label": "Connected Jira account",
    "my_jira.error.required": "Username and password are required.",
    "my_jira.error.save_failed":
      "Failed to connect Jira account. Please check your credentials.",

    // ── Jira connection banner ───────────────────────────────────────────────────
    "jira.banner.message":
      "Connect your Jira account in settings to access sprint data.",
    "jira.banner.action": "Configure Jira",

    // ── Jira Config – form labels ──────────────────────────────────────────────
    "jira.form.personal_label": "Personal Jira credentials",
    "jira.form.base_url": "Jira Base URL",
    "jira.form.auth_type": "Auth Type",
    "jira.form.user_email": "User Email / Login",
    "jira.form.username": "Jira Username",
    "jira.form.password_label": "Jira Password",
    "jira.form.project_key": "Project Key",
    "jira.form.board_id": "Board ID",

    // ── Jira Config – errors ───────────────────────────────────────────────────
    "jira.error.timeout": "Server did not respond. Is the backend running?",
    "jira.error.load_config": "Failed to load Jira configuration.",
    "jira.error.load_dashboards": "Failed to load dashboards.",
    "jira.error.base_url_token_required": "Base URL and token are required.",
    "jira.error.email_required_basic":
      "User email is required with BASIC auth.",
    "jira.error.base_url_required": "Jira Base URL is required.",
    "jira.error.email_required": "User email / login is required.",
    "jira.error.password_required": "Password is required.",
    "jira.error.test_failed":
      "Jira test failed. Please check credentials and board settings.",
    "jira.error.save_config": "Unable to save Jira configuration.",
    "jira.error.no_boards": "No boards found for this Jira account.",
    "jira.error.discover_boards": "Unable to discover Jira boards.",
    "jira.error.dashboard_fields_required":
      "Name, Project Key and Board ID are required.",
    "jira.error.dashboard_create": "Unable to create dashboard.",
    "jira.error.dashboard_limit":
      "Dashboard limit reached. Upgrade your plan to add more.",
    "jira.error.select_board": "Please select a board first.",
    "jira.error.dashboard_switch": "Unable to switch dashboard.",
    "jira.error.dashboard_delete": "Unable to delete dashboard.",
    "jira.error.username_password_required":
      "Username and password are required.",
    "jira.error.save_credentials": "Failed to save Jira credentials.",
    "jira.error.popup_blocked":
      "Pop-up blocked. Please allow pop-ups and retry.",
    "jira.error.oauth_url_missing": "OAuth URL was not provided by the server.",
    "jira.error.oauth_init": "Unable to initiate Jira OAuth.",
    "jira.error.oauth_disconnect": "Unable to disconnect Jira OAuth.",
    "jira.oauth.error": "Jira OAuth connection failed. Please retry.",

    // ── Jira Config – success ──────────────────────────────────────────────────
    "jira.success.oauth_connected": "Jira OAuth connected successfully.",
    "jira.success.config_saved": "Jira configuration saved and validated.",
    "jira.success.boards_discovered":
      "Boards discovered. Select one and create a dashboard.",
    "jira.success.dashboard_created": "Dashboard '{name}' created.",
    "jira.success.dashboard_activated": "Active dashboard updated.",
    "jira.success.dashboard_deleted": "Dashboard deleted.",
    "jira.success.personal_connected": "Jira account connected successfully.",
    "jira.success.oauth_disconnected": "Jira OAuth disconnected.",
    "jira.success.sync": "Sync completed.",
    "jira.dashboards.confirm.delete": "Delete dashboard '{name}'?",

    // ── Members – errors/success ───────────────────────────────────────────────
    "members.error.load": "Failed to load members.",
    "members.error.invalid_email": "Please enter a valid email address.",
    "members.error.invite_conflict":
      "This email is already invited or already a member.",
    "members.error.invite_failed": "Failed to send invitation.",
    "members.error.member_limit":
      "Member limit reached. Please upgrade your plan.",
    "members.error.bulk_failed": "Bulk invite failed.",
    "members.success.invited": "Invitation sent to {email}",

    // ── Shell – email verification banner ─────────────────────────────────────
    "shell.email_banner.message":
      "Please verify your email to activate all features.",
    "shell.email_banner.resend": "Resend link",
    "shell.email_banner.sending": "Sending…",
    "shell.email_banner.sent": "Verification email sent!",

    // ── Billing ────────────────────────────────────────────────────────────────
    "billing.title": "Billing & Subscription",
    "billing.subtitle": "Manage your subscription plan and billing.",
    "billing.no_plan": "No active subscription",
    "billing.no_plan_hint": "Choose a plan below to get started.",
    "billing.plans_title": "Available Plans",
    "billing.contact_us": "Contact Us",
    "billing.per_month": "/mo",
    "billing.current_plan": "Current Plan",
    "billing.renews": "Renews on",
    "billing.cancels": "Cancels on",
    "billing.cancel_btn": "Cancel Subscription",
    "billing.promo_title": "Have a promo code?",
    "billing.modal.cancel_title": "Cancel Subscription?",
    "billing.error.load": "Failed to load subscription.",
    "billing.error.load_plans": "Failed to load plans.",
    "billing.error.subscribe": "Subscription failed.",
    "billing.error.checkout": "Failed to start checkout.",
    "billing.error.redemption": "Redemption failed.",
    "billing.error.cancellation": "Cancellation failed.",

    // ── Auth – Login ───────────────────────────────────────────────────────────
    "auth.login.subtitle": "Sign in to your account",
    "auth.login.email_unverified":
      "Email address not verified. Please check your inbox.",
    "auth.login.resend_success": "Verification email sent! Check your inbox.",
    "auth.login.resend": "Resend confirmation email",
    "auth.login.resend_countdown": "Resend in {n}s",
    "auth.login.resend_sending": "Sending…",
    "auth.login.email_label": "Email",
    "auth.login.email_placeholder": "you@company.com",
    "auth.login.email_required": "Email is required.",
    "auth.login.email_invalid": "Please enter a valid email address.",
    "auth.login.password_label": "Password",
    "auth.login.password_required": "Password is required.",
    "auth.login.submit": "Sign in",
    "auth.login.signing_in": "Signing in…",
    "auth.login.no_account": "Don't have an account?",
    "auth.login.create_account": "Create one",

    // ── Auth – Register ────────────────────────────────────────────────────────
    "auth.register.title": "Create Account",
    "auth.register.subtitle": "Start your free trial",
    "auth.register.success_title": "Account created!",
    "auth.register.success_hint":
      "Check your inbox to verify your email before signing in.",
    "auth.register.signin_link": "Sign in",
    "auth.register.first_name": "First Name",
    "auth.register.last_name": "Last Name",
    "auth.register.email": "Email",
    "auth.register.password": "Password",
    "auth.register.org_name": "Organization Name",
    "auth.register.submit": "Create Account",
    "auth.register.creating": "Creating…",
    "auth.register.have_account": "Already have an account?",
    "auth.register.firstname_required": "First name is required.",
    "auth.register.firstname_min": "At least 2 characters required.",
    "auth.register.firstname_pattern":
      "Letters, spaces, hyphens and apostrophes only.",
    "auth.register.lastname_required": "Last name is required.",
    "auth.register.lastname_min": "At least 2 characters required.",
    "auth.register.lastname_pattern":
      "Letters, spaces, hyphens and apostrophes only.",
    "auth.register.email_required": "Email is required.",
    "auth.register.email_invalid": "Please enter a valid email address.",
    "auth.register.password_required": "Password is required.",
    "auth.register.password_min": "At least 8 characters required.",
    "auth.register.org_required": "Organization name is required.",
    "auth.register.invited_badge": "Invited",
    "auth.register.invited_hint":
      "You've been invited. Your account will join the existing organization.",
    "auth.register.invited_join": "You've been invited to join",
    "auth.register.invited_as": "as",
    "auth.register.invited_auto":
      "After verifying your email, your account will be linked automatically.",
    "auth.register.org_label": "Organization",
    "auth.register.already_verified": "Already verified?",
    "auth.register.checking": "Checking…",
    "auth.register.password_placeholder":
      "Min. 8 characters, 1 letter & 1 number",
    "auth.register.password_pattern":
      "Must contain at least one letter and one number.",
    "auth.register.email_placeholder": "you@company.com",

    // ── Auth – Verify Email ────────────────────────────────────────────────────
    "auth.verify.verifying": "Verifying your email…",
    "auth.verify.verifying_desc": "Please wait while we validate your link.",
    "auth.verify.success": "Email verified successfully",
    "auth.verify.success_desc":
      "Your email has been confirmed. You now have full access to all features.",
    "auth.verify.joined_title": "Welcome to the team!",
    "auth.verify.joined_desc":
      "Your email has been verified and your account is now linked to the organization as",
    "auth.verify.go_dashboard": "Go to Dashboard",
    "auth.verify.invalid": "Invalid verification link",
    "auth.verify.invalid_desc":
      "This link is invalid or has already been used. Please request a new verification email.",
    "auth.verify.expired": "Verification link expired",
    "auth.verify.expired_desc":
      "Your verification link has expired. Please request a new verification email.",
    "auth.verify.error": "Verification failed",
    "auth.verify.error_desc": "The link may be expired or invalid.",
    "auth.verify.signin": "Sign in",
    "auth.verify.back": "Back to Login",
    "auth.verify.resend": "Resend verification email",
    "auth.verify.resending": "Sending…",
    "auth.verify.resent": "Verification email resent!",

    // ── Common ────────────────────────────────────────────────────
    "common.export_csv": "Export CSV",
    "common.loading": "Loading…",
    "common.unlimited": "Unlimited",
    "common.premium": "★ Premium",
    "common.upgrade": "Upgrade to Premium",
  },

  fr: {
    // ── Navigation ────────────────────────────────────────────────
    "nav.dashboard": "Tableau de bord",
    "nav.backlog": "Backlog",
    "nav.metrics": "Métriques",
    "nav.capacity": "Capacité",
    "nav.settings": "Paramètres",
    "sidebar.no_org": "Aucune organisation",
    "sidebar.logout": "Déconnexion",

    // ── Backlog page ──────────────────────────────────────────────
    "backlog.title": "Backlog du sprint",
    "backlog.subtitle": "Suivre et gérer les tickets du sprint",

    // ── Metrics page ──────────────────────────────────────────────
    "metrics.title": "Métriques & Analytiques",
    "metrics.subtitle": "Vélocité, capacité et comparaisons historiques",
    "metrics.view.all": "Tout",
    "metrics.view.velocity": "Vélocité",
    "metrics.view.capacity": "Capacité",
    "metrics.view.topics": "Sujets",

    // ── Capacity page ─────────────────────────────────────────────
    "capacity.title": "Planification de capacité",
    "capacity.subtitle":
      "Gérer la disponibilité de l'équipe et planifier la capacité du sprint",

    // ── Sprint Dashboard ──────────────────────────────────────────
    "dashboard.brand_subtitle": "Espace de travail sprint",
    "dashboard.hero_kicker": "Automatisation du reporting agile",
    "dashboard.current_sprint": "Sprint en cours",
    "dashboard.tab.board": "Tableau",
    "dashboard.tab.metrics": "Métriques",
    "dashboard.tab.capacity": "Capacité",
    "dashboard.read_only": "Lecture seule",
    "dashboard.retry": "Réessayer",

    // ── Sprint Summary Card ───────────────────────────────────────
    "summary.total_issues": "Total tickets",
    "summary.total_sp": "Total SP",
    "summary.done_sp": "SP terminés",
    "summary.remaining_sp": "SP restants",
    "summary.completion": "Complétion",

    // ── Sprint Issue Table ────────────────────────────────────────
    "table.title": "Tickets du sprint",
    "table.key": "Clé",
    "table.summary": "Résumé",
    "table.assignee": "Responsable",
    "table.type": "Type",
    "table.status": "Statut",
    "table.total_sp": "Total SP",
    "table.remaining_sp": "SP restants",
    "table.done_sp": "SP terminés",
    "table.action": "Action",
    "table.empty": "Aucun ticket trouvé pour ce sprint.",
    "table.unassigned": "Non assigné",

    // ── Sprint Analytics ──────────────────────────────────────────
    "analytics.velocity.title": "Vélocité",
    "analytics.velocity.subtitle": "Engagé · Livré · Travail · Restant · Ratio",
    "analytics.capacity.title": "Capacité & Disponibilité équipe",
    "analytics.capacity.subtitle": "Planifié vs Réel · ETP par rôle",
    "analytics.topics.title": "Points par sujet",
    "analytics.topics.subtitle": "Répartition par epics / sujets",
    "analytics.iterations.title": "Comparaison des itérations",
    "analytics.iterations.sprint_col": "Sprint",
    "analytics.iterations.delivered_col": "Livré / Engagé",
    "analytics.iterations.velocity_col": "Vélocité",
    "analytics.iterations.ratio_col": "Ratio",
    "analytics.iterations.empty": "Aucun historique d'itération disponible",
    "analytics.ratio_label": "Ratio Livré / Engagé",
    "analytics.planned": "Planifié",
    "analytics.real": "Réel",
    "analytics.delta": "Écart",
    "analytics.loading": "Chargement des analytiques…",
    "analytics.team_availability": "Disponibilité équipe (ETP)",

    // ── Settings ──────────────────────────────────────────────────
    "settings.title": "Paramètres",
    "settings.subtitle": "Gérer la configuration de votre organisation",
    "settings.tab.jira": "Configuration Jira",
    "settings.tab.billing": "Abonnement & Facturation",
    "settings.tab.language": "Langue",
    "settings.tab.members": "Membres",

    // ── Language settings ─────────────────────────────────────────
    "lang.title": "Langue",
    "lang.subtitle": "Choisissez la langue préférée de l'application.",
    "lang.label": "Langue de l'interface",
    "lang.save": "Enregistrer la langue",
    "lang.saving": "Enregistrement...",
    "lang.saved": "Préférence de langue enregistrée.",
    "lang.error": "Échec de l'enregistrement de la préférence de langue.",
    "lang.loading": "Chargement de la préférence de langue...",

    // ── Members ───────────────────────────────────────────────────
    "members.title": "Membres",
    "members.desc":
      "Gérer les membres de l'équipe et les invitations en attente.",
    "members.filter.all": "Tous",
    "members.active.title": "Membres actifs",
    "members.active.empty": "Aucun membre actif trouvé.",
    "members.pending.title": "Invitations en attente",
    "members.pending.invited_on": "Invité le",
    "members.status.verified": "Vérifié",
    "members.status.pending": "En attente",
    "members.invite.title": "Inviter un membre",
    "members.invite.placeholder": "collegue@entreprise.com",
    "members.invite.sending": "Envoi…",
    "members.invite.send": "Inviter",
    "members.invite.error_required": "L'email est requis.",
    "members.invite.error_invalid": "Veuillez saisir une adresse email valide.",
    "members.bulk.title": "Invitation en masse",
    "members.bulk.hint":
      "Collez les emails séparés par des virgules, points-virgules ou sauts de ligne.",
    "members.bulk.sending": "Envoi…",
    "members.bulk.default_badge": "défaut",
    "members.bulk.dashboards_optional": "Tableaux de bord (optionnel)",
    "members.bulk.dashboards_selected": "sélectionnés",
    "members.bulk.result_title": "Invitations envoyées",
    "members.bulk.invited": "Invités",
    "members.bulk.already_pending": "Déjà en attente",
    "members.bulk.already_member": "Déjà membre",
    "members.bulk.invalid": "Invalides",
    "members.limit.upgrade": "Mettre à niveau",

    // ── Jira Config ───────────────────────────────────────────────
    "jira.title": "Configuration Jira",
    "jira.credentials.title": "Identifiants",
    "jira.credentials.connected": "Connecté",
    "jira.credentials.modify": "Modifier",
    "jira.credentials.cancel": "Annuler",
    "jira.credentials.save_test": "Enregistrer & Tester",
    "jira.credentials.saving": "Enregistrement & Test…",
    "jira.credentials.locked_hint":
      "Configurez et testez vos identifiants Jira en premier.",
    "jira.dashboards.title": "Tableaux de bord",
    "jira.dashboards.subtitle":
      "Le tableau de bord actif est utilisé par tous les utilisateurs pour charger les tickets du sprint. Le premier créé devient actif automatiquement.",
    "jira.dashboards.empty":
      "Aucun tableau de bord configuré. Ajoutez-en un ci-dessous.",
    "jira.dashboards.active": "Actif",
    "jira.dashboards.activate": "Activer",
    "jira.dashboards.add_title": "Ajouter un tableau de bord",
    "jira.dashboards.create": "Créer le tableau de bord",
    "jira.dashboards.creating": "Création…",
    "jira.dashboards.field_name": "Nom",
    "jira.dashboards.delete": "Supprimer ce tableau de bord",

    // ── My Jira Credentials (personal) ─────────────────────────────────────────────
    "settings.tab.my_jira": "Mon compte Jira",
    "my_jira.title": "Mon compte Jira",
    "my_jira.subtitle":
      "Connecte ton compte Jira personnel pour accéder aux données du sprint.",
    "my_jira.field.username": "Identifiant Jira (email ou login)",
    "my_jira.field.password": "Mot de passe ou Personal Access Token",
    "my_jira.save": "Connecter mon compte Jira",
    "my_jira.saving": "Connexion en cours…",
    "my_jira.success":
      "Compte Jira connecté. Redirection vers le tableau de bord…",
    "my_jira.connected_label": "Compte Jira connecté",
    "my_jira.error.required":
      "L’identifiant et le mot de passe sont obligatoires.",
    "my_jira.error.save_failed":
      "Impossible de connecter le compte Jira. Vérifie tes identifiants.",

    // ── Jira connection banner ───────────────────────────────────────────────────
    "jira.banner.message":
      "Connectez votre compte Jira dans les paramètres pour accéder aux données sprint.",
    "jira.banner.action": "Configurer Jira",

    // ── Jira Config – form labels ──────────────────────────────────────────────
    "jira.form.personal_label": "Identifiants Jira personnels",
    "jira.form.base_url": "URL de base Jira",
    "jira.form.auth_type": "Type d'authentification",
    "jira.form.user_email": "Email / Identifiant utilisateur",
    "jira.form.username": "Nom d'utilisateur Jira",
    "jira.form.password_label": "Mot de passe Jira",
    "jira.form.project_key": "Clé de projet",
    "jira.form.board_id": "ID du tableau",

    // ── Jira Config – errors ───────────────────────────────────────────────────
    "jira.error.timeout":
      "Le serveur ne répond pas. Le backend est-il démarré ?",
    "jira.error.load_config": "Impossible de charger la configuration Jira.",
    "jira.error.load_dashboards": "Impossible de charger les tableaux de bord.",
    "jira.error.base_url_token_required":
      "L'URL de base et le token sont requis.",
    "jira.error.email_required_basic":
      "L'email est requis pour l'authentification BASIC.",
    "jira.error.base_url_required": "L'URL de base Jira est requise.",
    "jira.error.email_required": "L'email / identifiant est requis.",
    "jira.error.password_required": "Le mot de passe est requis.",
    "jira.error.test_failed":
      "Test Jira échoué. Vérifiez vos identifiants et la configuration du tableau.",
    "jira.error.save_config": "Impossible d'enregistrer la configuration Jira.",
    "jira.error.no_boards": "Aucun tableau trouvé pour ce compte Jira.",
    "jira.error.discover_boards": "Impossible de découvrir les tableaux Jira.",
    "jira.error.dashboard_fields_required":
      "Le nom, la clé de projet et l'ID du tableau sont requis.",
    "jira.error.dashboard_create": "Impossible de créer le tableau de bord.",
    "jira.error.dashboard_limit":
      "Limite de tableaux de bord atteinte. Mettez à niveau votre plan.",
    "jira.error.select_board": "Veuillez d'abord sélectionner un tableau.",
    "jira.error.dashboard_switch": "Impossible de changer de tableau de bord.",
    "jira.error.dashboard_delete":
      "Impossible de supprimer le tableau de bord.",
    "jira.error.username_password_required":
      "L'identifiant et le mot de passe sont requis.",
    "jira.error.save_credentials":
      "Impossible d'enregistrer les identifiants Jira.",
    "jira.error.popup_blocked":
      "Pop-up bloqué. Veuillez autoriser les pop-ups et réessayer.",
    "jira.error.oauth_url_missing":
      "L'URL OAuth n'a pas été fournie par le serveur.",
    "jira.error.oauth_init":
      "Impossible d'initier l'authentification OAuth Jira.",
    "jira.error.oauth_disconnect": "Impossible de déconnecter l'OAuth Jira.",
    "jira.oauth.error": "Connexion OAuth Jira échouée. Veuillez réessayer.",

    // ── Jira Config – success ──────────────────────────────────────────────────
    "jira.success.oauth_connected": "Connexion OAuth Jira réussie.",
    "jira.success.config_saved": "Configuration Jira enregistrée et validée.",
    "jira.success.boards_discovered":
      "Tableaux découverts. Sélectionnez-en un et créez un tableau de bord.",
    "jira.success.dashboard_created": "Tableau de bord '{name}' créé.",
    "jira.success.dashboard_activated": "Tableau de bord actif mis à jour.",
    "jira.success.dashboard_deleted": "Tableau de bord supprimé.",
    "jira.success.personal_connected": "Compte Jira connecté avec succès.",
    "jira.success.oauth_disconnected": "OAuth Jira déconnecté.",
    "jira.success.sync": "Synchronisation terminée.",
    "jira.dashboards.confirm.delete": "Supprimer le tableau de bord '{name}' ?",

    // ── Members – errors/success ───────────────────────────────────────────────
    "members.error.load": "Impossible de charger les membres.",
    "members.error.invalid_email": "Veuillez saisir une adresse email valide.",
    "members.error.invite_conflict":
      "Cet email est déjà invité ou déjà membre.",
    "members.error.invite_failed": "Impossible d'envoyer l'invitation.",
    "members.error.member_limit":
      "Limite de membres atteinte. Mettez à niveau votre plan.",
    "members.error.bulk_failed": "Invitation en masse échouée.",
    "members.success.invited": "Invitation envoyée à {email}",

    // ── Shell – email verification banner ─────────────────────────────────────
    "shell.email_banner.message":
      "Veuillez vérifier votre email pour activer toutes les fonctionnalités.",
    "shell.email_banner.resend": "Renvoyer le lien",
    "shell.email_banner.sending": "Envoi…",
    "shell.email_banner.sent": "Email de vérification envoyé !",

    // ── Billing ────────────────────────────────────────────────────────────────
    "billing.title": "Abonnement & Facturation",
    "billing.subtitle": "Gérez votre plan d'abonnement et la facturation.",
    "billing.no_plan": "Aucun abonnement actif",
    "billing.no_plan_hint": "Choisissez un plan ci-dessous pour commencer.",
    "billing.plans_title": "Plans disponibles",
    "billing.contact_us": "Contactez-nous",
    "billing.per_month": "/mois",
    "billing.current_plan": "Plan actuel",
    "billing.renews": "Renouvellement le",
    "billing.cancels": "Annulation le",
    "billing.cancel_btn": "Annuler l'abonnement",
    "billing.promo_title": "Vous avez un code promo ?",
    "billing.modal.cancel_title": "Annuler l'abonnement ?",
    "billing.error.load": "Impossible de charger l'abonnement.",
    "billing.error.load_plans": "Impossible de charger les plans.",
    "billing.error.subscribe": "Échec de l'abonnement.",
    "billing.error.checkout": "Impossible de démarrer le paiement.",
    "billing.error.redemption": "Échec de la rédemption.",
    "billing.error.cancellation": "Échec de l'annulation.",

    // ── Auth – Login ───────────────────────────────────────────────────────────
    "auth.login.subtitle": "Se connecter à votre compte",
    "auth.login.email_unverified":
      "Adresse email non vérifiée. Vérifiez votre boîte de réception.",
    "auth.login.resend_success":
      "Email de vérification envoyé ! Vérifiez votre boîte de réception.",
    "auth.login.resend": "Renvoyer l'email de confirmation",
    "auth.login.resend_countdown": "Renvoyer dans {n}s",
    "auth.login.resend_sending": "Envoi…",
    "auth.login.email_label": "Email",
    "auth.login.email_placeholder": "vous@entreprise.com",
    "auth.login.email_required": "L'email est requis.",
    "auth.login.email_invalid": "Veuillez saisir une adresse email valide.",
    "auth.login.password_label": "Mot de passe",
    "auth.login.password_required": "Le mot de passe est requis.",
    "auth.login.submit": "Se connecter",
    "auth.login.signing_in": "Connexion en cours…",
    "auth.login.no_account": "Pas encore de compte ?",
    "auth.login.create_account": "Créer un compte",

    // ── Auth – Register ────────────────────────────────────────────────────────
    "auth.register.title": "Créer un compte",
    "auth.register.subtitle": "Démarrer votre essai gratuit",
    "auth.register.success_title": "Compte créé !",
    "auth.register.success_hint":
      "Vérifiez votre boîte de réception pour confirmer votre email avant de vous connecter.",
    "auth.register.signin_link": "Se connecter",
    "auth.register.first_name": "Prénom",
    "auth.register.last_name": "Nom",
    "auth.register.email": "Email",
    "auth.register.password": "Mot de passe",
    "auth.register.org_name": "Nom de l'organisation",
    "auth.register.submit": "Créer le compte",
    "auth.register.creating": "Création…",
    "auth.register.have_account": "Vous avez déjà un compte ?",
    "auth.register.firstname_required": "Le prénom est requis.",
    "auth.register.firstname_min": "Au moins 2 caractères requis.",
    "auth.register.firstname_pattern":
      "Lettres, espaces, tirets et apostrophes uniquement.",
    "auth.register.lastname_required": "Le nom est requis.",
    "auth.register.lastname_min": "Au moins 2 caractères requis.",
    "auth.register.lastname_pattern":
      "Lettres, espaces, tirets et apostrophes uniquement.",
    "auth.register.email_required": "L'email est requis.",
    "auth.register.email_invalid": "Veuillez saisir une adresse email valide.",
    "auth.register.password_required": "Le mot de passe est requis.",
    "auth.register.password_min": "Au moins 8 caractères requis.",
    "auth.register.org_required": "Le nom de l'organisation est requis.",
    "auth.register.invited_badge": "Invité(e)",
    "auth.register.invited_hint":
      "Vous avez été invité(e). Votre compte rejoindra l'organisation existante.",
    "auth.register.invited_join": "Vous avez été invité(e) à rejoindre",
    "auth.register.invited_as": "en tant que",
    "auth.register.invited_auto":
      "Après la vérification de votre email, votre compte sera lié automatiquement.",
    "auth.register.org_label": "Organisation",
    "auth.register.already_verified": "Déjà vérifié ?",
    "auth.register.checking": "Vérification…",
    "auth.register.password_placeholder":
      "Min. 8 caractères, 1 lettre & 1 chiffre",
    "auth.register.password_pattern":
      "Doit contenir au moins une lettre et un chiffre.",
    "auth.register.email_placeholder": "vous@entreprise.com",

    // ── Auth – Verify Email ────────────────────────────────────────────────────
    "auth.verify.verifying": "Vérification de votre email…",
    "auth.verify.verifying_desc":
      "Veuillez patienter pendant la validation de votre lien.",
    "auth.verify.success": "Email vérifié avec succès",
    "auth.verify.success_desc":
      "Votre email a été confirmé. Vous avez maintenant un accès complet à toutes les fonctionnalités.",
    "auth.verify.joined_title": "Bienvenue dans l'équipe !",
    "auth.verify.joined_desc":
      "Votre email a été vérifié et votre compte est maintenant lié à l'organisation en tant que",
    "auth.verify.go_dashboard": "Accéder au tableau de bord",
    "auth.verify.invalid": "Lien de vérification invalide",
    "auth.verify.invalid_desc":
      "Ce lien est invalide ou a déjà été utilisé. Veuillez demander un nouvel email de vérification.",
    "auth.verify.expired": "Lien de vérification expiré",
    "auth.verify.expired_desc":
      "Votre lien de vérification a expiré. Veuillez en demander un nouveau.",
    "auth.verify.error": "Échec de la vérification",
    "auth.verify.error_desc": "Le lien est peut-être expiré ou invalide.",
    "auth.verify.signin": "Se connecter",
    "auth.verify.back": "Retour à la connexion",
    "auth.verify.resend": "Renvoyer l'email de vérification",
    "auth.verify.resending": "Envoi…",
    "auth.verify.resent": "Email de vérification renvoyé !",

    // ── Common ────────────────────────────────────────────────────
    "common.export_csv": "Exporter CSV",
    "common.loading": "Chargement…",
    "common.unlimited": "Illimité",
    "common.premium": "★ Premium",
    "common.upgrade": "Mettre à niveau vers Premium",
  },
};
