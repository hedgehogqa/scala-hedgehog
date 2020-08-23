const algoliaConfig = require('./algolia.config.json');

const isEmptyObject = obj => {
  for (field in obj) return false;
  return true;
};

const isSearchable = !isEmptyObject(algoliaConfig)

const websiteConfig = {
  title: 'Hedgehog',
  tagline: 'Modern property-based testing system for Scala',
  url: 'https://hedgehogqa.github.io',
  baseUrl: '/scala-hedgehog/',
  onBrokenLinks: 'throw',
  favicon: 'img/favicon.png',
  organizationName: 'hedgehogqa', // Usually your GitHub org/user name.
  projectName: 'scala-hedgehog', // Usually your repo name.
  themeConfig: {
    sidebarCollapsible: false,
    colorMode: {
      respectPrefersColorScheme: true,
    },
    prism: {
      theme: require('prism-react-renderer/themes/nightOwl'),
      darkTheme: require('prism-react-renderer/themes/nightOwl'),
      additionalLanguages: ['scala', 'haskell'],
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
          href: 'https://github.com/hedgehogqa/scala-hedgehog',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
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
              to: 'docs/integration-minitest/',
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
          // It is recommended to set document id as docs home page (`docs/` path).
          homePageId: 'hedgehog',
          sidebarPath: require.resolve('./sidebars.js'),
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      },
    ],
  ],
};

if (isSearchable) {
  websiteConfig['themeConfig']['algolia'] = algoliaConfig;
}

module.exports = websiteConfig;
