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
    },
    timeZone: "Africa/Douala",
  };
});
