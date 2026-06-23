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
  },
};
