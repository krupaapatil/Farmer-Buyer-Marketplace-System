export const ROUTES = new Set(["/", "/login", "/signup", "/dashboard", "/add-crops", "/buy-crops", "/profile"]);
export const AUTH_ROUTES = new Set(["/dashboard", "/add-crops", "/buy-crops", "/profile"]);

export const DEFAULT_CROP_TYPES = [
  "Wheat",
  "Rice",
  "Cotton",
  "Sugarcane",
  "Maize",
  "Tomato",
  "Potato",
  "Onion"
];

export const DEFAULT_UNITS = ["kg", "quintal", "ton"];
export const DEFAULT_STATUSES = ["active", "fulfilled", "expired"];
export const DEFAULT_ROLES = ["farmer", "buyer", "both"];

export const ROUTE_META = {
  "/dashboard": {
    title: "Home Dashboard",
    subtitle: ""
  },
  "/add-crops": {
    title: "Add Crops",
    subtitle: ""
  },
  "/buy-crops": {
    title: "Buy Crops",
    subtitle: ""
  },
  "/profile": {
    title: "Profile",
    subtitle: ""
  }
};

export const BREAKPOINTS = {
  xs: 360,
  md: 768,
  lg: 1024,
  xl: 1440
};
