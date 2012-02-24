AUI.add(
	'liferay-form-navigator',
	function(A) {
		var AObject = A.Object;

		var CSS_HIDDEN = 'aui-helper-hidden-accessible';

		var CSS_SECTION_ERROR = 'section-error';

		var CSS_SELECTED = 'selected';

		var History = Liferay.HistoryManager;

		var SELECTOR_FORM_SECTION = '.form-section';

		var SELECTOR_LIST_ITEM_SELECTED = 'li.selected';

		var SELECTOR_SECTION_ERROR = '.' + CSS_SECTION_ERROR;

		var STR_HREF = 'href';

		var FormNavigator = function(options) {
			var instance = this;

			instance._namespace = options.namespace || '';

			instance._container = A.one(options.container);

			instance._formName = options.formName;

			Liferay.after('form:registered', instance._afterFormRegistered, instance);

			instance._navigation = instance._container.one('.form-navigator');
			instance._sections = instance._container.all(SELECTOR_FORM_SECTION);

			if (instance._navigation) {
				instance._navigation.delegate('click', instance._onClick, 'li a', instance);
			}

			if (options.modifiedSections) {
				instance._modifiedSections = A.all('[name=' + options.modifiedSections+ ']');

				if (!instance._modifiedSections) {
					instance._modifiedSections = A.Node.create('<input name="' + options.modifiedSections + '" type="hidden" />');

					instance._container.append(instance._modifiedSections);
				}
			}
			else {
				instance._modifiedSections = null;
			}

			if (options.defaultModifiedSections) {
				instance._modifiedSectionsArray = options.defaultModifiedSections;
			}
			else {
				instance._modifiedSectionsArray = [];
			}

			instance._revealSection();

			A.on('formNavigator:trackChanges', instance._trackChanges, instance);

			var inputs = instance._container.all('input, select, textarea');

			if (inputs) {
				inputs.on(
					'change',
					function(event) {
						A.fire('formNavigator:trackChanges', event.target);
					}
				);
			}

			Liferay.on(
				'submitForm',
				function(event, data) {
					if (instance._modifiedSections) {
						instance._modifiedSections.val(instance._modifiedSectionsArray.join(','));
					}
				}
			);

			History.after('stateChange', instance._afterStateChange, instance);
		};

		FormNavigator.prototype = {
			constructor: FormNavigator,

			_addHistoryState: function(data) {
				var instance = this;

				var historyState = A.clone(data);

				var currentHistoryState = History.get();

				var owns = AObject.owns;

				AObject.each(
					currentHistoryState,
					function(index, item, collection) {
						if (!owns(historyState, item)) {
							historyState[item] = null;
						}
					}
				);

				if (!AObject.isEmpty(historyState)) {
					History.add(historyState);
				}
			},

			_afterStateChange: function(event) {
				var instance = this;

				var section = History.get(instance._namespace + instance._sectionParam);

				if (section) {
					instance._revealSection(section);
				}
			},

			_addModifiedSection: function (section) {
				var instance = this;

				if (A.Array.indexOf(section, instance._modifiedSectionsArray) == -1) {
					instance._modifiedSectionsArray.push(section);
				}
			},

			_afterFormRegistered: function(event) {
				var instance = this;

				if (event.formName === instance._formName) {
					var formValidator = event.form.formValidator;

					instance._formValidator = formValidator;

					formValidator.on(['errorField', 'validField'], instance._updateSectionStatus, instance);

					formValidator.on('submitError', instance._revealSectionError, instance);
				}
			},

			_getSectionName: function(elementHref) {
				var sectionMatch = elementHref.split('#');

				var sectionName;

				if (sectionMatch) {
					sectionName = sectionMatch[1];
				}

				return sectionName;
			},

			_onClick: function(event) {
				var instance = this;

				event.preventDefault();

				var target = event.currentTarget;

				var li = target.get('parentNode');

				if (li && !li.test('.selected')) {
					var href = target.attr(STR_HREF);

					var sectionName = instance._getSectionName(href);

					instance._revealSection(sectionName, li);

					var data = {};

					data[instance._namespace + instance._sectionParam] = sectionName;

					instance._addHistoryState(data);
				}
			},

			_revealSection: function(sectionName, currentNavItem) {
				var instance = this;

				if (!sectionName) {
					sectionName = History.get(instance._namespace + instance._sectionParam);
				}

				if (sectionName) {
					var li = currentNavItem || instance._navigation.one('[href$=' + sectionName + ']').get('parentNode');

					var section = A.one('#' + sectionName);
					var selected = instance._navigation.one(SELECTOR_LIST_ITEM_SELECTED);

					var revealed = false;

					if (li && (!selected || selected !== li)) {
						if (selected) {
							selected.removeClass(CSS_SELECTED);
						}

						li.addClass(CSS_SELECTED);

						revealed = true;
					}

					if (section && !section.hasClass(CSS_SELECTED)) {
						instance._sections.removeClass(CSS_SELECTED).addClass(CSS_HIDDEN);

						section.addClass(CSS_SELECTED).removeClass(CSS_HIDDEN);

						revealed = true;
					}

					if (revealed) {
						Liferay.fire('formNavigator:reveal' + sectionName);
					}
				}
			},

			_revealSectionError: function() {
				var instance = this;

				var sectionError = instance._navigation.one(SELECTOR_SECTION_ERROR);

				var sectionErrorLink = sectionError.one('a').attr(STR_HREF);

				instance._revealSection(sectionErrorLink, sectionError);
			},

			_trackChanges: function(el) {
				var instance = this;

				var currentSection = A.one(el).ancestor(SELECTOR_FORM_SECTION).attr('id');

				var currentSectionLink = A.one('#' + currentSection + 'Link');

				if (currentSectionLink) {
					currentSectionLink.get('parentNode').addClass('section-modified');
				}

				instance._addModifiedSection(currentSection);
			},

			_updateHash: function(section) {
				var instance = this;

				location.hash = instance._hashKey + section;
			},

			_updateSectionStatus: function() {
				var instance = this;

				var navigation = instance._navigation;

				var lis = navigation.all('li');

				lis.removeClass(CSS_SECTION_ERROR);

				var formValidator = instance._formValidator;

				if (formValidator.hasErrors()) {
					var selectors = A.Object.keys(formValidator.errors);

					A.all('#' + selectors.join(', #')).each(
						function(item, index, collection) {
							var section = item.ancestor(SELECTOR_FORM_SECTION);

							if (section) {
								var navItem = navigation.one('a[href="#' + section.attr('id') + '"]');

								if (navItem) {
									navItem.ancestor().addClass(CSS_SECTION_ERROR);
								}
							}
						}
					);
				}
			},

			_hashKey: '_LFR_FN_',

			_sectionParam: 'section'
		};

		Liferay.FormNavigator = FormNavigator;
	},
	'',
	{
		requires: ['aui-base', 'liferay-history-manager']
	}
);