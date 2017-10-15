# Foodie
An example of [GraphQL](https://facebook.github.io/graphql) server supporting [Relay modern](https://facebook.github.io/relay/docs/relay-modern.html) written with [Play framework](https://www.playframework.com), [Sangria](http://sangria-graphql.org) and [Slick](http://slick.lightbend.com).

## Usage
1. Create a database named _foodie_ and run `sbt "runMain models.mains.SlickDatabaseGenerator"` to generate the required tables in it.
1. Run `sbt "runMain models.mains.GenerateSchemaGraphql"` to generate the GraphQL schema in the file, _ui/src/data/schema.graphql_. This file is required by [Relay Compiler](https://facebook.github.io/relay/docs/relay-compiler.html).
1. Go to folder _ui_ and run `npm install` to install the npm packages.
1. Run `npm run relay` to generate the [files](https://facebook.github.io/relay/docs/relay-compiler.html#source-files) required by [Relay Babel plugin](https://facebook.github.io/relay/docs/babel-plugin-relay.html).
1. Run `npm run build` to bundle the UI source code.
1. Run the web server using `sbt run` and go to [http://localhost:9000](http://localhost:9000) for the app and go to [http://localhost:9000/graphiql](http://localhost:9000/graphiql) for [GraphiQL](https://github.com/graphql/graphiql).
> Note: The default database configuration is based on MySQL. You can change the database configuration in the file, _conf/application.conf_.