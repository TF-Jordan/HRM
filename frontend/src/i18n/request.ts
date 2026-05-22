import { hasLocale } from "next-intl";
import { getRequestConfig } from "next-intl/server";
import { routing } from "./routing";

export default getRequestConfig(async ({ requestLocale }) => {
  const requested = await requestLocale;
  const locale = hasLocale(routing.locales, requested) ? requested : routing.defaultLocale;

  const [
    common,
    auth,
    validation,
    errors,
    statuses,
    navigation,
    dashboard,
    employees,
    selfService,
    manager,
    accounting,
    payrollOfficer,
    recruitment,
    trainings,
    trainingBudgets,
    skills,
    analytics,
    medical,
    admin,
  ] = await Promise.all([
    import(`./messages/${locale}/common.json`).then((m) => m.default),
    import(`./messages/${locale}/auth.json`).then((m) => m.default),
    import(`./messages/${locale}/validation.json`).then((m) => m.default),
    import(`./messages/${locale}/errors.json`).then((m) => m.default),
    import(`./messages/${locale}/statuses.json`).then((m) => m.default),
    import(`./messages/${locale}/navigation.json`).then((m) => m.default),
    import(`./messages/${locale}/dashboard.json`).then((m) => m.default),
    import(`./messages/${locale}/employees.json`).then((m) => m.default),
    import(`./messages/${locale}/self-service.json`).then((m) => m.default),
    import(`./messages/${locale}/manager.json`).then((m) => m.default),
    import(`./messages/${locale}/accounting.json`).then((m) => m.default),
    import(`./messages/${locale}/payroll-officer.json`).then((m) => m.default),
    import(`./messages/${locale}/recruitment.json`).then((m) => m.default),
    import(`./messages/${locale}/trainings.json`).then((m) => m.default),
    import(`./messages/${locale}/training-budgets.json`).then((m) => m.default),
    import(`./messages/${locale}/skills.json`).then((m) => m.default),
    import(`./messages/${locale}/analytics.json`).then((m) => m.default),
    import(`./messages/${locale}/medical.json`).then((m) => m.default),
    import(`./messages/${locale}/admin.json`).then((m) => m.default),
  ]);

  return {
    locale,
    messages: {
      common,
      auth,
      validation,
      errors,
      statuses,
      navigation,
      dashboard,
      employees,
      selfService,
      manager,
      accounting,
      payrollOfficer,
      recruitment,
      trainings,
      trainingBudgets,
      skills,
      analytics,
      medical,
      admin,
    },
    timeZone: "Africa/Douala",
  };
});
