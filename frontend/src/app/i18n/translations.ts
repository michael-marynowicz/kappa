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

    // ── Common ────────────────────────────────────────────────────
    "common.export_csv": "Export CSV",
    "common.loading": "Loading…",
    "common.premium": "★ Premium",
    "common.upgrade": "Upgrade to Premium",

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

    // ── Common ────────────────────────────────────────────────────
    "common.export_csv": "Exporter CSV",
    "common.loading": "Chargement…",
    "common.premium": "★ Premium",
    "common.upgrade": "Mettre à niveau vers Premium",

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
  },
};
