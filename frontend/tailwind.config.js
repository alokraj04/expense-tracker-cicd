/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        dark: {
          bg: '#0A1A14',
          card: '#102A20',
          border: '#1B4332',
          surface: '#173628'
        },
        primary: {
          DEFAULT: '#2D6A4F',
          hover: '#40916C',
          light: '#52B788'
        },
        danger: '#FF4D4D',
        warning: '#F59E0B',
        success: '#10B981',
      },
      fontFamily: {
        heading: ['Syne', 'sans-serif'],
        body: ['DM Sans', 'sans-serif'],
      }
    },
  },
  plugins: [],
}
