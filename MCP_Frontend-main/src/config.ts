export const config = {
  apiUrl: 'http://ui-lib-demo-api.herokuapp.com',
  apiBackEndUrl: 'https://mighty-hamlet-08807.herokuapp.com/api/v1',
  apiBackEndUrlProd: 'https://mighty-hamlet-08807.herokuapp.com/api/v1',
  authRoles: {
    sa: ['SA'], // Only Super Admin has access
    admin: ['SA', 'Admin'], // Only SA & Admin has access
    editor: ['SA', 'Admin', 'Editor'], // Only SA & Admin & Editor has access
    user: ['SA', 'Admin', 'Editor', 'User'], // Only SA & Admin & Editor & User has access
    guest: ['SA', 'Admin', 'Editor', 'User', 'Guest'] // Everyone has access
  }
}