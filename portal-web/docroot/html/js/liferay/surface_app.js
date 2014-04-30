AUI.add(
	'liferay-surface-app',
	function(A) {
		var Surface = Liferay.Surface;

		Surface.app = new A.SurfaceApp(
			{
				basePath: Surface.getBasePath(),
				linkSelector: 'a:not(.portlet-icon-back):not([data-navigation]):not([data-resource-href])',
				on: {
					endNavigate: function(event) {
						var instance = this;

						if (Surface.isActionURL(event.path)) {
							var redirect = Surface.getRedirect(event.path);

							var title = instance.get('title');

							Surface.sendRedirect(redirect, title);
						}

						Liferay.fire(
							'surfaceEndNavigate',
							{
								app: Surface.app,
								path: event.path
							}
						);

						A.one('body').removeClass('lfr-surface-loading');
					},
					startNavigate: function(event) {
						var instance = this;

						if (Surface.isActionURL(event.path)) {
							event.path = Surface.isolatePortletURLRedirect(event.path);
						}

						Liferay.fire(
							'surfaceStartNavigate',
							{
								app: Surface.app,
								path: event.path
							}
						);

						A.one('body').addClass('lfr-surface-loading');
					}
				}
			}
		);

		Surface.app.addScreenRoutes(
			[
				{
					path: function(url) {
						if (url.search(Surface.getPatternPortletURL(Liferay.PortletURL.ACTION_PHASE)) > -1) {
							return true;
						}

						return false;
					},
					screen: Surface.ActionURLScreen
				},
				{
					path: Surface.getPatternFriendlyURL(),
					screen: Surface.RenderURLScreen
				},
				{
					path: function(url) {
						if (url.search(Surface.getPatternPortletURL(Liferay.PortletURL.RENDER_PHASE)) > -1) {
							return true;
						}

						return false;
					},
					screen: Surface.RenderURLScreen
				}
			]
		);

		Surface.app.addSurfaces(Surface.getSurfaceIds());

		Liferay.on(
			'closePortlet',
			function(event) {
				var portletId = event.portletId;

				var surfaceId = Surface.getPortletBoundaryId(portletId);

				delete Surface.app.surfaces[surfaceId];
			}
		);

		Liferay.on(
			'portletReady',
			function(event) {
				var portletId = event.portletId;

				var surfaceId = Surface.getPortletBoundaryId(portletId);

				if (Surface.isAllowedPortletId(portletId) && !Surface.app.surfaces[surfaceId]) {
					Surface.app.addSurfaces(surfaceId);
				}
			}
		);

		Liferay.on(
			'surfaceStartNavigate',
			function(event) {
				Surface.resetAllPortlets();
			}
		);
	},
	'',
	{
		requires: [ 'liferay-surface' ]
	}
);