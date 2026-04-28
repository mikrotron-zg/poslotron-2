import org.apache.ofbiz.entity.Delegator
import org.apache.ofbiz.entity.GenericValue
import org.apache.ofbiz.entity.util.EntityQuery
import org.apache.ofbiz.base.util.UtilDateTime
import org.apache.ofbiz.webapp.OfbizUrlBuilder

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.zip.GZIPOutputStream

String generate() {

    // Resolve the public base URL for the webapp's WebSite (which is bound to
    // the default ProductStore via WebSite.productStoreId). Falls back to
    // url.properties (force.https.host / port.https) when the WebSite entity
    // does not override them.
    StringBuilder hostBuf = new StringBuilder()
    OfbizUrlBuilder.from(request).buildHostPart(hostBuf, "", true)
    String baseUrl = hostBuf.toString()

    // -------------------------------------------------
    // gzip support
    // -------------------------------------------------
    boolean gzip = false
    String acceptEncoding = request.getHeader("Accept-Encoding")
    if (acceptEncoding && acceptEncoding.toLowerCase().contains("gzip")) {
        gzip = true
    }

    if (gzip) {
        response.setHeader("Content-Encoding", "gzip")
    }

    response.setContentType("application/xml; charset=UTF-8")

    def outputStream = gzip ?
            new GZIPOutputStream(response.outputStream) :
            response.outputStream

    def out = new OutputStreamWriter(outputStream, "UTF-8")

    out << '<?xml version="1.0" encoding="UTF-8"?>\n'
    out << '<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9"\n'
    out << '        xmlns:image="http://www.google.com/schemas/sitemap-image/1.1">\n'

    // -------------------------------------------------
    // HOME PAGE
    // -------------------------------------------------
    writeUrl(out,
            baseUrl + "/",
            today(),
            "daily",
            "1.0",
            null)

    // -------------------------------------------------
    // STATIC PAGES
    // -------------------------------------------------
    ["/faq", "/policies", "/AnonContactus"].each { p ->
        writeUrl(out,
                baseUrl + p,
                today(),
                "monthly",
                "0.6",
                null)
    }

    // -------------------------------------------------
    // PRODUCTS
    // only active + saleable
    // -------------------------------------------------
    Timestamp now = UtilDateTime.nowTimestamp()

    List<GenericValue> products = EntityQuery.use(delegator)
            .from("Product")
            .where(
                    "isVirtual", "N",
                    "salesDiscontinuationDate", null
            )
            .queryList()

    products.each { product ->

        String productId = product.productId
        if (!productId) return

        Timestamp introDate = product.introductionDate
        if (introDate && introDate.after(now)) return

        // product url
        String productUrl = baseUrl + "/product/" + productId

        // last modified
        String lastmod = formatDate(product.lastUpdatedStamp ?: product.createdStamp ?: now)

        // image
        String imageUrl = findProductImage(delegator, productId, baseUrl)

        writeUrl(out,
                productUrl,
                lastmod,
                "weekly",
                "0.8",
                imageUrl)
    }

    // -------------------------------------------------
    // CATEGORIES
    // -------------------------------------------------
    List<GenericValue> cats = EntityQuery.use(delegator)
            .from("ProductCategory")
            .queryList()

    cats.each { cat ->

        String catId = cat.productCategoryId
        if (!catId) return

        String url = baseUrl + "/category?category_id=" + catId
        String lastmod = formatDate(cat.lastUpdatedStamp ?: now)

        writeUrl(out,
                url,
                lastmod,
                "weekly",
                "0.7",
                null)
    }

    out << '</urlset>'
    out.flush()
    out.close()

    return "success"
}

// -------------------------------------------------
// write one url block
// -------------------------------------------------
void writeUrl(def out,
              String loc,
              String lastmod,
              String freq,
              String priority,
              String imageUrl) {

    out << "<url>\n"
    out << "  <loc>${xml(loc)}</loc>\n"
    out << "  <lastmod>${lastmod}</lastmod>\n"
    out << "  <changefreq>${freq}</changefreq>\n"
    out << "  <priority>${priority}</priority>\n"

    if (imageUrl) {
        out << "  <image:image>\n"
        out << "    <image:loc>${xml(imageUrl)}</image:loc>\n"
        out << "  </image:image>\n"
    }

    out << "</url>\n"
}

// -------------------------------------------------
// product image lookup
// -------------------------------------------------
String findProductImage(Delegator delegator,
                        String productId,
                        String baseUrl) {

    try {
        GenericValue content = EntityQuery.use(delegator)
                .from("ProductContentAndInfo")
                .where(
                        "productId", productId,
                        "productContentTypeId", "LARGE_IMAGE_URL"
                )
                .queryFirst()

        if (content && content.contentPathPrefix) {
            return baseUrl + content.contentPathPrefix
        }

        GenericValue p = EntityQuery.use(delegator)
                .from("Product")
                .where("productId", productId)
                .queryOne()

        if (p?.largeImageUrl) {
            return baseUrl + p.largeImageUrl
        }

    } catch (Exception ignored) {
    }

    return null
}

// -------------------------------------------------
// helpers
// -------------------------------------------------
String today() {
    return formatDate(new Timestamp(System.currentTimeMillis()))
}

String formatDate(Timestamp ts) {
    return new SimpleDateFormat("yyyy-MM-dd").format(ts)
}

String xml(String s) {
    if (!s) return ""
    return s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
}
