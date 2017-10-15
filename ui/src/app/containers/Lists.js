import Lists from '../components/Lists';
import {createFragmentContainer, graphql} from 'react-relay/index';

export default createFragmentContainer(Lists,
  graphql`
    fragment Lists_store on Query {
      foods (first: 2147483647) @connection(key: "FoodsList_foods") {
        edges {
          node {
            name
            id
            type {
              name
              id
            }
          }
          cursor
        }
      }
      types (first: 2147483647) @connection(key: "TypesList_types") {
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
);