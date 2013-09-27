AUI.add(
	'liferay-staging',
	function(A) {
		var Lang = A.Lang;

		var StagingBar = {
			init: function(config) {
				var instance = this;

				var namespace = config.namespace;

				instance._groupId = config.groupId;

				instance._isPending = config.isPending;

				instance._namespace = namespace;

				instance._stagingBar = A.oneNS(namespace, '#stagingBar');

				instance._stagingNavEl = A.oneNS(namespace, '#stagingNavEl');

				instance._taskExecutorClassName = config.taskExecutorClassName;

				instance._bindUI();

				Liferay.publish(
					{
						fireOnce: true
					}
				);

				Liferay.after(
					'initStagingBar',
					function(event) {
						A.getBody().addClass('staging-ready');
					}
				);

				Liferay.fire('initStagingBar', config);
			},

			_bindUI: function() {
				var instance = this;

				var stagingBar = instance._stagingBar;

				if (stagingBar) {
					stagingBar.delegate(
						'change',
						function(event) {
							A.config.win.location.href = event.currentTarget.val();
						},
						'select.variation-options'
					);
				}

				var stagingNavEl = instance._stagingNavEl;

				if (stagingNavEl) {
					stagingNavEl.on(
						'click',
						function(event) {
							var retValue = 0;

							Liferay.Service(
								'/backgroundtask/get-background-tasks-count',
								{
									groupId: instance._groupId,
									taskExecutorClassName: instance._taskExecutorClassName,
									completed: instance._isPending
								},
								function(obj) {
									retValue = obj;
								}
							);

							if (retValue > 0) {
								event.preventDefault();

								if (!instance._notice) {
									instance._notice = new Liferay.Notice(
										{
											closeText: false,
											content: Liferay.Language.get('an-inital-staging-publication-is-in-progress') + '<button type="button" class="close" />',
											toggleText: false,
											timeout: 10000,
											type: 'warning',
											useAnimation: true
										}
									)
								}

								instance._notice.show();
							}
						}
					);
				}
			}
		};

		Liferay.StagingBar = StagingBar;
	},
	'',
	{
		requires: ['aui-io-plugin-deprecated', 'liferay-node', 'liferay-util-window']
	}
);