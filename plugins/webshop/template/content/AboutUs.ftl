<div class="card">
    <div class="card-header">
        ${uiLabelMap.EcommerceAboutUs}
    </div>

    <div class="card-body text-secondary">
    <#if logoImageUrl?has_content>
        <img src="<@ofbizContentUrl>${logoImageUrl}</@ofbizContentUrl>" overflow="hidden" height="40px" content-height="scale-to-fit" content-width="150px"/>
    </#if>
    <div>
        <#--${companyName}-->
    </div>
    <ul class="list-group list-group-flush">
        <li class="list-group-item">Mikrotron d.o.o.</li>
        <li class="list-group-item">Pakoštanska 5 K2-9</li>
        <li class="list-group-item">10000 Zagreb</li>
        <li class="list-group-item">Hrvatska</li>
        <li class="list-group-item">OIB: 43227166836</li>
        <li class="list-group-item">VAT ID: HR43227166836</li>
        <li class="list-group-item">
            <a href="tel:385017999194">
                <span class="bi-telephone-fill"></span> +385 (0)1 7999 194
            </a>
        </li>
        <li class="list-group-item">
            <a href="mailto:mikrotron@mikrotron.hr">
                <span class="bi-envelope-fill"></span> mikrotron@mikrotron.hr
            </a>
        </li>
        <li class="list-group-item">
            <a href="http://www.mikrotron.hr" target="_blank">
                <span class="bi-link-45deg"></span> www.mikrotron.hr
            </a>
        </li>
        <li class="list-group-item"><span class="bi-bank"></span> Privredna Banka Zagreb</li>
        <li class="list-group-item">SWIFT: PBZGHR2X</li>
        <li class="list-group-item">IBAN: HR8023400091110675464</li>
    </ul>
    </div>
</div>