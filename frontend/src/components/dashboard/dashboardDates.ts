export const dashboardDates = new Intl.DateTimeFormat("en-GB", {
  timeZone: "Europe/Berlin",
  dateStyle: "medium",
});

export function formatDeliveryDate(value: string) {
  return dashboardDates.format(new Date(value + "T12:00:00Z"));
}
