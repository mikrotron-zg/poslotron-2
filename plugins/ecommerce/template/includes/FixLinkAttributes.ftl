<#--
Custom Freemarker functions to fix link attributes in HTML content.
This directive will:
1. Preserve target="_blank" attributes on links
2. Add rel="noopener noreferrer" to links with target="_blank" if rel is not present
3. Replace rel="nofollow" with rel="noopener noreferrer" on external links

Usage:
<#assign fixedHtml = fixLinkAttributes(productContentWrapper.get("LONG_DESCRIPTION", "html")!)>
${fixedHtml}

Or use the macro for block content:
<@fixLinkAttributesBlock content=productContentWrapper.get("LONG_DESCRIPTION", "html")! />
-->

<#function fixLinkAttributes html="">
  <#if html?has_content>
    <#-- Use regex to handle various attribute combinations -->
    <#-- Pattern 1: target="_blank" without any rel attribute -->
    <#assign html = html?replace('(<a[^>]*target="_blank")(?![^>]*rel=)([^>]*>)', '$1 rel="noopener noreferrer"$2', 'r')>
    
    <#-- Pattern 2: rel="nofollow" with target="_blank" (order 1) -->
    <#assign html = html?replace('target="_blank" rel="nofollow"', 'target="_blank" rel="noopener noreferrer"')>
    
    <#-- Pattern 3: rel="nofollow" with target="_blank" (order 2) -->
    <#assign html = html?replace('rel="nofollow" target="_blank"', 'target="_blank" rel="noopener noreferrer"')>
    
    <#-- Pattern 4: standalone rel="nofollow" (for external links without target) -->
    <#-- Only replace if it's an external link (contains http:// or https://) -->
    <#assign html = html?replace('(<a[^>]*href="https?://[^"]*"[^>]*)rel="nofollow"([^>]*>)', '$1rel="noopener noreferrer"$2', 'r')>
  </#if>
  <#return html>
</#function>

<#--
Macro version for block content
-->
<#macro fixLinkAttributesBlock content="">
  <#if content?has_content>
    <#-- Use regex to handle various attribute combinations -->
    <#-- Pattern 1: target="_blank" without any rel attribute -->
    <#assign fixedContent = content?replace('(<a[^>]*target="_blank")(?![^>]*rel=)([^>]*>)', '$1 rel="noopener noreferrer"$2', 'r')>
    
    <#-- Pattern 2: rel="nofollow" with target="_blank" (order 1) -->
    <#assign fixedContent = fixedContent?replace('target="_blank" rel="nofollow"', 'target="_blank" rel="noopener noreferrer"')>
    
    <#-- Pattern 3: rel="nofollow" with target="_blank" (order 2) -->
    <#assign fixedContent = fixedContent?replace('rel="nofollow" target="_blank"', 'target="_blank" rel="noopener noreferrer"')>
    
    <#-- Pattern 4: standalone rel="nofollow" (for external links without target) -->
    <#-- Only replace if it's an external link (contains http:// or https://) -->
    <#assign fixedContent = fixedContent?replace('(<a[^>]*href="https?://[^"]*"[^>]*)rel="nofollow"([^>]*>)', '$1rel="noopener noreferrer"$2', 'r')>
    
    ${fixedContent}
  </#if>
</#macro>
