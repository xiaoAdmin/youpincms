package com.youpin.cms

import javax.servlet.http.HttpServletRequest;



class LoginFilters {
    
	def filters = {
		all(controller:'*', action:'*') {
			before = {
				HttpServletRequest rs;
				//rs.getContextPath()
				def uri=request.getRequestURI()
				def url=request.getRequestURL()
			  
				if(uri.contains(request.getContextPath())){
					return true
				}
				if(url.indexOf("index.gsp")>0){
					return true
				}
				if (!session.user && !actionName.equals('login')) {
					redirect(controller: 'user', action: 'login')
					return false
				}
				return true
			}
		}
	}
}
