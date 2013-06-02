class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/"{
            controller = "goods"
        }
		"500"(view:'/error')
	}
}
