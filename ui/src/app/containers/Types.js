import Types from '../components/Types';
import {createRefetchContainer, graphql} from 'react-relay';

export default createRefetchContainer(Types,
  {
    typeStore: graphql`
      fragment Types_typeStore on Query
      @argumentDefinitions(
        sort: {type: "String", defaultValue: "desc"}
      ) {
        types (first: 2147483647, sort: $sort) @connection(key: "TypesList_types", filters: []) {
          edges {
            node {
              name
              id
            }
            cursor
          }
        }
      }
    `
  },
  graphql`
    query TypesRefetchQuery($sort: String) {
      ...Types_typeStore @arguments(sort: $sort) 
    }
  `,
);