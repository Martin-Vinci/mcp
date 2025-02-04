// The file contents for the current environment will overwrite these during build.
// The build system defaults to the dev environment which uses `environment.ts`, but if you do
// `ng build --env=prod` then `environment.prod.ts` will be used instead.
// The list of which env maps to which file can be found in `angular.json`.


export const environment = {
  production: false,
  // apiBackEndUrl: 'http://127.0.0.1:9001/api/v1',
  //apiBackEndUrl: 'https://41.210.172.245:8036/mcp-apis-test/api/v1',
  apiBackEndUrl: 'https://41.210.172.245:9001/mcp-apis-prod/api/v1'
};