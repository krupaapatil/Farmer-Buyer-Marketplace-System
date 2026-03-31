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
    subtitle: "Track live opportunities, active demand, and the activity that moves your marketplace forward."
  },
  "/add-crops": {
    title: "Add Crops",
    subtitle: "Create polished listings with validation, then review buyer demand from the same workspace."
  },
  "/buy-crops": {
    title: "Buy Crops",
    subtitle: "Post structured purchase requests and review current supply without switching layouts."
  },
  "/profile": {
    title: "Profile",
    subtitle: "Keep your account, role, and contact details ready for every listing and request."
  }
};

export const BREAKPOINTS = {
  xs: 360,
  md: 768,
  lg: 1024,
  xl: 1440
};
