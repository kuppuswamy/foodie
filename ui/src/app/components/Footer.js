import React from 'react';

export default class Footer extends React.Component {
  render = () => (
    <footer className="footer">
      <div className="container">
        <div className="content has-text-centered">
          <p>
            <strong>Foodie</strong>, an example of <a href="https://facebook.github.io/graphql">GraphQL</a> server
            supporting <a href="https://facebook.github.io/relay/docs/relay-modern.html">Relay modern</a> written with
            {' '}<a href="https://www.playframework.com">Play framework</a>, <a
            href="http://sangria-graphql.org">Sangria</a>
            {' '}and <a href="http://slick.lightbend.com">Slick</a>.
          </p>
        </div>
      </div>
    </footer>
  )
}