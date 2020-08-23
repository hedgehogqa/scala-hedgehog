import React from 'react';
import clsx from 'clsx';
import Layout from '@theme/Layout';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import useBaseUrl from '@docusaurus/useBaseUrl';
import styles from './styles.module.css';

const features = [
  {
    title: <>Simplified Failures</>,
    imageUrl: 'img/shrink.svg',
    description: (
      <>
        Hedgehog's integrated shrinkers are composed automatically
        when you compose generators, so test failures are automatically simplified.
      </>
    ),
  },
  {
    title: <>Range Control</>,
    imageUrl: 'img/radar.svg',
    description: (
      <>
        Range combinators for full control over the scope of generated numbers and collections.
      </>
    ),
  },
  {
    title: <>State Machine Testing</>,
    imageUrl: 'img/change.svg',
    description: (
      <>
        Test stateful systems like web services and database layers with
        state-based property testing.
      </>
    ),
  },
];

function Feature({imageUrl, title, description}) {
  const imgUrl = useBaseUrl(imageUrl);
  return (
    <div className={clsx('col col--4', styles.feature)}>
      {imgUrl && (
        <div className="text--center">
          <img className={styles.featureImage} src={imgUrl} alt={title} />
        </div>
      )}
      <h3>{title}</h3>
      <p>{description}</p>
    </div>
  );
}

function Home() {
  const context = useDocusaurusContext();
  const {siteConfig = {}} = context;
  return (
    <Layout
      title={`${siteConfig.title} for Scala`}
      description="Modern property-based testing system for Scala">
      <header className={clsx('hero hero--primary', styles.heroBanner)}>
        <div className="container">
          <img src={`${useBaseUrl('img/')}/poster.png`} alt="Hedgehog Logo" />
          <h1 className="hero__title">{siteConfig.title}</h1>
          <p className="hero__subtitle">{siteConfig.tagline}</p>
          <div className={styles.buttons}>
            <Link
              className={clsx(
                'button button--outline button--secondary button--lg',
                styles.getStarted,
              )}
              to={useBaseUrl('docs/')}>
              Get Started
            </Link>
          </div>
        </div>
      </header>
      <main>
        {features && features.length > 0 && (
          <section className={styles.features}>
            <div className="container">
              <div className="row">
                {features.map((props, idx) => (
                  <Feature key={idx} {...props} />
                ))}
              </div>
            </div>
          </section>
        )}
      </main>
    </Layout>
  );
}

export default Home;
