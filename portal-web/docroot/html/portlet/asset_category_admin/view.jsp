<%--
/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
--%>

<%@ include file="/html/portlet/asset_category_admin/init.jsp" %>

<aui:form name="fm">
	<aui:nav-bar>
		<aui:nav>
			<c:if test="<%= AssetPermission.contains(permissionChecker, themeDisplay.getSiteGroupId(), ActionKeys.ADD_VOCABULARY) %>">
				<aui:nav-item id="addVocabularyButton" label="add-vocabulary" />
			</c:if>

			<c:if test="<%= AssetPermission.contains(permissionChecker, themeDisplay.getSiteGroupId(), ActionKeys.ADD_CATEGORY) %>">
				<aui:nav-item disabled="<%= true %>" id="addCategoryButton" label="add-category" />
			</c:if>

			<c:if test="<%= AssetPermission.contains(permissionChecker, themeDisplay.getSiteGroupId(), ActionKeys.PERMISSIONS) && GroupPermissionUtil.contains(permissionChecker, themeDisplay.getSiteGroupId(), ActionKeys.PERMISSIONS) %>">
				<liferay-security:permissionsURL
					modelResource="com.liferay.portlet.asset"
					modelResourceDescription="<%= themeDisplay.getScopeGroupName() %>"
					resourcePrimKey="<%= String.valueOf(themeDisplay.getSiteGroupId()) %>"
					var="permissionsURL"
					windowState="<%= LiferayWindowState.POP_UP.toString() %>"
				/>

				<aui:nav-item data-url="<%= permissionsURL %>" id="categoryPermissionsButton" label="permissions" />
			</c:if>

			<aui:nav-item dropdown="<%= true %>" label="actions">
				<aui:nav-item iconCssClass="icon-remove" id="deleteSelectedItems" label="delete" />
			</aui:nav-item>
		</aui:nav>

		<aui:nav-bar-search cssClass="pull-right">
			<aui:select cssClass="categories-admin-select-search" label="" name="categoriesAdminSelectSearch">
				<aui:option label="categories" />
				<aui:option label="vocabularies" selected="<%= true %>" />
			</aui:select>

			<liferay-ui:input-search cssClass="form-search" id="categoriesAdminSearchInput" name="tagsAdminSearchInput" showButton="<%= false %>" />
		</aui:nav-bar-search>
	</aui:nav-bar>

	<aui:row cssClass="categories-admin-content">
		<aui:col cssClass="vocabulary-list-container" width="<%= 25 %>">
			<span class="select-vocabularies-container">
				<aui:input cssClass="select-vocabularies" inline="<%= true %>" label="" name="checkAllVocabularies" title='<%= LanguageUtil.get(pageContext, "check-all-vocabularies") %>' type="checkbox" />
			</span>

			<h3 class="vocabularies-header"><%= LanguageUtil.get(pageContext, "vocabularies") %></h3>

					<div class="unstyled vocabulary-message"></div>

					<div class="unstyled vocabulary-list"></div>

			<div class="vocabularies-pagination"></div>

			<script type="text/javascript" src="http://cdnjs.cloudflare.com/ajax/libs/jquery/1.7.1/jquery.min.js"></script>

			<script type="text/javascript">
				/**
 * jQuery plugin for getting position of cursor in textarea

 * @license under Apache license
 * @author Bevis Zhao (i@bevis.me, http://bevis.me)
 */

(function($, window, document, undefined){
	$(function() {
		var calculator = {
			// key styles
			primaryStyles: ['fontFamily', 'fontSize', 'fontWeight', 'fontVariant', 'fontStyle',
				'paddingLeft', 'paddingTop', 'paddingBottom', 'paddingRight',
				'marginLeft', 'marginTop', 'marginBottom', 'marginRight',
				'borderLeftColor', 'borderTopColor', 'borderBottomColor', 'borderRightColor',
				'borderLeftStyle', 'borderTopStyle', 'borderBottomStyle', 'borderRightStyle',
				'borderLeftWidth', 'borderTopWidth', 'borderBottomWidth', 'borderRightWidth',
				'line-height', 'outline'],

			specificStyle: {
				'white-space': 'pre-wrap',
				'word-wrap': 'break-word',
				'overflow-x': 'hidden',
				'overflow-y': 'auto'
			},

			simulator : $('<div id="textarea_simulator"/>').css({
					position: 'absolute',
					top: 0,
					left: 0,
					visibility: 'hidden'
				}).appendTo(document.body),

			toHtml : function(text) {
				return text;
			},
			// calculate position
			getCaretPosition: function() {
				var cal = calculator, self = this, element = self[0], elementOffset = self.offset();

				// IE has easy way to get caret offset position
				if ($.browser.msie) {
					// must get focus first
					element.focus();
				    var range = document.selection.createRange();
				    return {
				        left: range.boundingLeft - elementOffset.left,
				        top: parseInt(range.boundingTop) - elementOffset.top + element.scrollTop
							+ document.documentElement.scrollTop + parseInt(self.getComputedStyle("fontSize"))
				    };
				}
				cal.simulator.empty();
				// clone primary styles to imitate textarea
				$.each(cal.primaryStyles, function(index, styleName) {
					self.cloneStyle(cal.simulator, styleName);
				});

				// caculate width and height
				cal.simulator.css($.extend({
					'width': self.width(),
					'height': self.height()
				}, cal.specificStyle));

				var value = self.val(), cursorPosition = self.getCursorPosition();
				var beforeText = value.substring(0, cursorPosition),
					afterText = value.substring(cursorPosition);

				var before = cal.toHtml(beforeText),
					focus = $('<span class="focus"/>'),
					after = cal.toHtml(afterText);

				cal.simulator.append(before).append(focus).append(after);
				var focusOffset = focus.offset(), simulatorOffset = cal.simulator.offset();
				// alert(focusOffset.left  + ',' +  simulatorOffset.left + ',' + element.scrollLeft);
				return {
					top: focusOffset.top - simulatorOffset.top - element.scrollTop
						// calculate and add the font height except Firefox
						+ parseInt(self.getComputedStyle("fontSize")),
					left: focus[0].offsetLeft -  cal.simulator[0].offsetLeft - element.scrollLeft
				};
			}
		};

		$.fn.extend({
			getComputedStyle: function(styleName) {
				if (this.length == 0) return;
				var thiz = this[0];
				var result = this.css(styleName);
				result = result || ($.browser.msie ?
					thiz.currentStyle[styleName]:
					document.defaultView.getComputedStyle(thiz, null)[styleName]);
				return result;
			},
			// easy clone method
			cloneStyle: function(target, styleName) {
				var styleVal = this.getComputedStyle(styleName);
				if (!!styleVal) {
					$(target).css(styleName, styleVal);
				}
			},
			cloneAllStyle: function(target, style) {
				var thiz = this[0];
				for (var styleName in thiz.style) {
					var val = thiz.style[styleName];
					typeof val == 'string' || typeof val == 'number'
						? this.cloneStyle(target, styleName)
						: NaN;
				}
			},
			getCursorPosition : function() {
		        var thiz = this[0], result = 0;
		        if ('selectionStart' in thiz) {
		            result = thiz.selectionStart;
		        } else if('selection' in document) {
		        	var range = document.selection.createRange();
		        	if (parseInt($.browser.version) > 6) {
			            thiz.focus();
			            var length = document.selection.createRange().text.length;
			            range.moveStart('character', - thiz.value.length);
			            result = range.text.length - length;
		        	} else {
		                var bodyRange = document.body.createTextRange();
		                bodyRange.moveToElementText(thiz);
		                for (; bodyRange.compareEndPoints("StartToStart", range) < 0; result++)
		                	bodyRange.moveStart('character', 1);
		                for (var i = 0; i <= result; i ++){
		                    if (thiz.value.charAt(i) == '\n')
		                        result++;
		                }
		                var enterCount = thiz.value.split('\n').length - 1;
						result -= enterCount;
	                    return result;
		        	}
		        }
		        return result;
		    },
			getCaretPosition: calculator.getCaretPosition
		});
	});
})(jQuery, window, document);


			</script>
			<script type="text/javascript">
				$(function() {
					var tip = $('#tip');
					$('#textarea').bind('keyup', function(e) {
						if (e.keyCode == 65) {
							var pos = $(this).getCaretPosition();
							tip.css({
								left: this.offsetLeft + pos.left,
								top: this.offsetTop + pos.top
							}).show();
						}
					})
				});
			</script>


			<h2>Input 'a' in textarea for test!</h2>
			<textarea id="textarea" rows='3' style='width:415px;' style='resize:none'></textarea>
			<div id="tip" style='width:20px; height:5px; background-color:red; display:none; position:absolute;background-color:red;font-size:0;'></div>

		</aui:col>

		<aui:col cssClass="vocabulary-categories-container" width="<%= 40 %>">
			<span class="select-vocabularies-container">
				<aui:input cssClass="select-categories" inline="<%= true %>" label="" name="checkAllCategories" title='<%= LanguageUtil.get(pageContext, "check-all-categories") %>' type="checkbox" />
			</span>

			<h3 class="categories-header"><%= LanguageUtil.get(pageContext, "categories") %></h3>

			<div class="vocabulary-categories"></div>
		</aui:col>

		<aui:col cssClass="hide vocabulary-edit-category" width="<%= 35 %>">
			<h3><%= LanguageUtil.get(pageContext, "category-details") %></h3>

			<aui:button cssClass="category-view-close close" icon="icon-remove" />

			<div class="category-view"></div>
		</aui:col>
	</aui:row>
</aui:form>

<aui:script use="liferay-category-admin">
	new Liferay.Portlet.AssetCategoryAdmin(
		{
			baseActionURL: '<%= PortletURLFactoryUtil.create(request, portletDisplay.getId(), themeDisplay.getPlid(), PortletRequest.ACTION_PHASE) %>',
			baseRenderURL: '<%= PortletURLFactoryUtil.create(request, portletDisplay.getId(), themeDisplay.getPlid(), PortletRequest.RENDER_PHASE) %>',
			itemsPerPage: <%= SearchContainer.DEFAULT_DELTA %>,
			portletId: '<%= portletDisplay.getId() %>'
		}
	);
</aui:script>