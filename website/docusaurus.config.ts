import {themes as prismThemes} from 'prism-react-renderer';
import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';


const algoliaConfig = require('./algolia.config.json');
const googleAnalyticsConfig = require('./google-analytics.config.json');

const lightCodeTheme = prismThemes.nightOwlLight;
const darkCodeTheme = prismThemes.nightOwl;

const isEmptyObject = (obj: object) => Object.keys(obj).length === 0;

const isSearchable = !isEmptyObject(algoliaConfig);
const hasGoogleAnalytics = !isEmptyObject(googleAnalyticsConfig);

const baseUrl = '/scala-hedgehog/'

const websiteConfig = {
  title: 'Hedgehog',
  tagline: 'Modern property-based testing system for Scala',
  url: 'https://hedgehogqa.github.io',
  baseUrl: baseUrl,
  onBrokenLinks: 'throw',
  favicon: 'img/favicon.png',
  organizationName: 'hedgehogqa', // Usually your GitHub org/user name.
  projectName: 'scala-hedgehog', // Usually your repo name.
  themeConfig: {
    colorMode: {
      respectPrefersColorScheme: true,
    },
    prism: {
      theme: lightCodeTheme,
      darkTheme: darkCodeTheme,
      additionalLanguages: ['java', 'scala', 'haskell'],
    },
    navbar: {
      title: 'Hedgehog',
      logo: {
        alt: 'Hedgehog Logo',
        src: 'img/hedgehog-logo-32x32.png',
      },
      items: [
        {
          to: 'docs/',
          activeBasePath: 'docs',
          label: 'Docs',
          position: 'left',
        },
        {
          href: `${baseUrl}api`,
          label: 'API',
          target: '_blank',
          position: 'left',
        },
        {
          href: 'https://github.com/hedgehogqa/scala-hedgehog',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    separateCss: ["api"],
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Docs',
          items: [
            {
              label: 'Docs',
              to: 'docs/',
            },
            {
              label: 'Guides',
              to: 'docs/guides/',
            },
            {
              label: 'Integration',
              to: 'docs/integration/',
            },
          ],
        },
        {
          title: 'More',
          items: [
            {
              label: 'GitHub',
              href: 'https://github.com/hedgehogqa/scala-hedgehog',
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} Hedgehog, Website built with Docusaurus.<br /><div>Some icons made by <a href="https://www.flaticon.com/authors/darius-dan" title="Darius Dan">Darius Dan</a> and <a href="https://www.flaticon.com/authors/pixel-perfect" title="Pixel perfect">Pixel perfect</a> from <a href="https://www.flaticon.com/" title="Flaticon"> www.flaticon.com</a>`,
    },
  },
  presets: [
    [
      '@docusaurus/preset-classic',
      {
        docs: {
          path: '../generated-docs/target/mdoc/',
          sidebarPath: require.resolve('./sidebars.js'),
          sidebarCollapsible: false,
          sidebarCollapsed: false,
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      },
    ],
  ],
  plugins: [
    require.resolve('docusaurus-lunr-search'),
  ],
};

if (isSearchable) {
  websiteConfig['themeConfig']['algolia'] = algoliaConfig;
}
if (hasGoogleAnalytics) {
  websiteConfig['themeConfig']['googleAnalytics'] = googleAnalyticsConfig;
}

module.exports = websiteConfig;
