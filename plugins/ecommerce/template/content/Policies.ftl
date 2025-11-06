<#--
This file is part of Poslotron project by Mikrotron d.o.o.
licensed under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

<div class="card">
  <div class="card-header">
    <b>
      <#if (locale == "hr")>
        Opći uvjeti poslovanja
      <#else>
        Store policies
      </#if>
    </b>
  </div>

  <div class="card-body text-secondary">
    <div>
      <#if (locale == "hr")>
        <h5>Sadržaj:</h5>
          <ol>
            <a href="#general"><li class="mb-1">Opće odredbe</li></a>
            <a href="#prices"><li class="mb-1">Cijene</li></a>
            <a href="#payment"><li class="mb-1">Načini plaćanja</li></a>
            <a href="#order"><li class="mb-1">Narudžba</li></a>
            <a href="#returns"><li class="mb-1">Povrat robe</li></a>
            <a href="#complaints"><li class="mb-1">Reklamacije, garancija i servis</li></a>
            <a href="#privacy"><li class="mb-1">Izjava o privatnosti</li></a>
          </ol>
      <#else>
        <h5>Content:</h5>
          <ol>
            <a href="#general"><li class="mb-1">General Terms and Conditions</li></a>
            <a href="#prices"><li class="mb-1">Prices</li></a>
            <a href="#payment"><li class="mb-1">Payment</li></a>
            <a href="#order"><li class="mb-1">Order</li></a>
            <a href="#returns"><li class="mb-1">Returns</li></a>
            <a href="#complaints"><li class="mb-1">Complaints, Warranty and Service</li></a>
            <a href="#privacy"><li class="mb-1">Privacy Statement</li></a>
          </ol>
      </#if>
    </div>
    <ul class="list-group list-group-flush">
      <li id="general" class="list-group-item">
        <#if (locale == "hr")>
          <h4>1. Opće odredbe</h4>
          <p>
            Mikrotron d.o.o. za trgovinu i usluge (u daljem tekstu: Mikrotron) pruža online usluge putem svojih
            internet stranica.
          </p>
          <p>
            Uvjeti korištenja propisuju uvjete i pravila za korisnike web stranica.
            Korištenjem stranica korisnici potvrđuju da se slažu s Općim uvjetima poslovanja.
            Pravo korištenja je osobno i neotuđivo pravo korisnika.
            Krajnji korisnik osobno je odgovoran za zaštitu povjerljivosti lozinke, na mjestima 
            gdje one kao takve postoje.
          </p>
          <p>
            Mikrotron nije odgovoran za bilo kakav gubitak podataka na internetu, ne snosi
            odgovornost za posljedice koje mogu nastati uporabom web stranica, i ne jamči za točnost i pouzdanost
            robe i informacija dane preko stranica. Mikrotron zadržava pravo u bilo kojem trenutku
            izmjeniti ili ukinuti bilo koji segment poslovanja, uključujući informacije i sadržaj web stranica, te
            opće uvjete poslovanja, bez obveze informiranja korisnika. Mikrotron može raskinuti poslovni odnos s 
            bilo kojim korisnikom u bilo kojem trenutku bez posebnog obrazloženja.
          </p>
          <p>
            Sav sadržaj na web stranicama eksluzivno je vlasništvo Mikrotrona ili se koristi uz odobrenje
            nositelja autorskih prava, i podliježe zakonu o autorskim pravima. Bilo kakva reprodukcija bez
            pismenog odobrenja Mikrotrona je zabranjena.
          </p>
        <#else>
          <h4>1. General Terms and Conditions</h4>
          <p>
            Mikrotron d.o.o. for trade and services (hereinafter: Mikrotron) provides online services through its 
            websites.
          </p>
          <p>
            The terms of use prescribe the conditions and rules for website users. By using the site, users confirm
            that they agree with the General Terms and Conditions. The right of use is a personal and inalienable 
            right of the user. The end user is personally responsible for protecting the confidentiality of passwords, 
            where they exist as such.
          </p>
          <p>
            Mikrotron is not responsible for any loss of data on the Internet, is not responsible for the consequences 
            that may arise from the use of websites, and does not guarantee the accuracy and reliability of goods and 
            information provided through the pages. Mikrotron reserves the right to change or terminate any segment of 
            the business at any time, including the information and content of the web pages, as well as the general 
            terms and conditions of business, without the obligation to inform users. Mikrotron may terminate the business
            relationship with any user at any time without special explanation.
          </p>
          <p>
            All content on the website is the exclusive property of Mikrotron or is used with the permission of the 
            copyright holder, and is subject to copyright law. Any reproduction without the written permission of Mikrotron 
            is prohibited.
          </p>
        </#if>
      </li>
      <li id="prices" class="list-group-item">
        <#if (locale == "hr")>
          <h4>2. Cijene</h4>
          <p> 
            Cijene su izražene u <b>eurima (EUR) i uključuju PDV</b>. Valuta plaćanja za kupce iz Hrvatske i 
            inozemstva su euri (EUR). Mikrotron zadržava pravo promjene cijena bez prethodne najave - 
            cijene ovise o dobavljačima, proizvođačima, tečajevima, troškovima dostave, carine itd.
          </p>
        <#else>
          <h4>2. Prices</h4>
          <p>
            Prices are expressed in euros (EUR) and include VAT. The currency of payment for customers from 
            Croatia and abroad is euros (EUR). Mikrotron reserves the right to change prices without prior notice - 
            prices depend on suppliers, manufacturers, exchange rates, shipping costs, customs, etc.
          </p>
        </#if>
      </li>
      <li id="payment" class="list-group-item">
        <#if (locale == "hr")>
          <h4>3. Načini plaćanja</h4>
          <p>
            Uplate prema narudžbi ili ponudi primamo u eurima (EUR) isključivo na žiro račun: <br>
            <b>IBAN HR8023400091110675464</b><br>
            otvoren kod Privredne banke Zagreb. Plaćanje možete izvršiti općom uplatnicom, internet
            ili mobilnim bankarstvom, kao i putem servisa za plaćanje (npr. PayPal ili Aircash). Sva 
            plaćanja su jednokratna, plaćanje nije moguće u ratama. Uplate u gotovini ili karticama 
            nismo u mogućnosti primiti.
          </p>
          <p>
            Proizvode isporučujemo po zaprimljenoj uplati. Ako naručenu robu preuzimate osobno u našem uredu,
            molimo:
            <ul class="pl-lg-5">
              <li>nazovite prije dolaska ukoliko dolazite izvan vremena za preuzimanje navedenog na narudžbenici</li>
              <li>sa sobom ponesite ili pošaljite mailom potvrdu o plaćanju ukoliko narudžbu želite podići isti dan 
              kada ste izvršili uplatu</li>
            </ul>
          </p>
        <#else>
          <h4>3. Payment</h4>
          <p>
            We accept payments according to the order or offer in euros (EUR) exclusively to the company bank account:<br>
            <b>IBAN HR8023400091110675464</b><br>
            at Privredna banka Zagreb. You can make the payment by general money order, internet or mobile banking, as via 
            a payment service (e.g. PayPal or Aircash). All payments are one-time, payment in installments is not possible. 
            We are unable to accept cash or card payments.
          </p>
          <p>
            We deliver the products upon receipt of payment. If you pick up the ordered goods in person at our office, please:
            <ul class="pl-lg-5">
              <li>call before arrival if you are coming outside the pick-up time stated on the purchase order</li>
              <li>bring with you a confirmation of payment (or send it by e-mail) if you want to pick up the order on the same
              day you made the payment</li>
            </ul>
          </p>
        </#if>
      </li>
      <li id="order" class="list-group-item">
        <#if (locale == "hr")>
          <h4>4. Narudžba</h4>
          <p>
            Nakon izdavanja narudžbe, roba po narudžbi je rezervirana. Ukoliko ste naručili robu koju trenutno nemamo 
            na skladištu ili u količini većoj nego što je trenutno raspoloživa, kontaktirati ćemo Vas kako bi se 
            dogovorili o roku i načinu isporuke. Narudžbenica je važeća 14 dana, nakon toga ćemo ju stornirati 
            bez prethodne obavijesti. Ako ne želite da se to dogodi, molimo Vas da nas kontaktirate prije
            isteka gore navedenog roka. Skladište se razdužuje kada primimo uplatu i izdamo račun. 
          </p>
        <#else>
          <h4>4. Order</h4>
          <p>
            After placing the order, the ordered goods are reserved. If you have ordered goods that we do not currently have
            in stock or in a larger quantity than is currently available, we will contact you to agree on the delivery date 
            and method. The purchase order is valid for 14 days, after which we will cancel it without prior notice. 
            If you do not want this to happen, please contact us before the above deadline expires. The goods are 
            discharged from the warehouse when we receive the payment and issue an invoice.
          </p>
        </#if>
      </li>
      <li id="returns" class="list-group-item">
        <#if (locale == "hr")>
          <h4>5. Povrat robe</h4>
          <p>
            U slučaju da ste iz bilo kojeg razloga nezadovoljni proizvodom, isti možete zamijeniti ili vratiti u 
            zamjenu za novac u roku od 14 dana od dana primitka naručene robe. Za povrat robe nije potrebno navoditi 
            nikakav razlog, a za ostvarivanje prava na povrat robe u zamjenu za novac potrebno je poslati obavijest 
            o povratu robe na e-mail <a href="mailto:diykits.shop@mikrotron.hr">diykits.shop@mikrotron.hr</a> i neoštećeni proizvod 
            poslati na našu adresu u originalnom pakiranju i sa svom opremom i dokumentacijom s kojim je proizvod bio 
            originalno isporučen. Troškove pošiljke u slučaju povrata i zamjene snosi kupac. Mikrotron će izvršiti 
            povrat uplate (uključujući i originalne troškove isporuke) nakon zaprimanja i provjere vraćenog proizvoda.
          </p>
        <#else>
          <h4>5. Returns</h4>
          <p>
            In case you are not satisfied with the product for any reason, you can exchange it or return it for money 
            within 14 days from the day of receipt of the ordered goods. There is no need to state any reason for the 
            return of the goods, and to exercise the right to return the goods in exchange for money, it is necessary 
            to send a notification about the return of the goods to e-mail <a href="mailto:diykits.shop@mikrotron.hr">diykits.shop@mikrotron.hr</a>
            and send the undamaged product to our address in the original packaging and with all equipment and the 
            documentation with which the product was originally delivered. Shipping costs in case of returns and exchanges 
            are borne by the buyer. Mikrotron will issue a refund (including original shipping costs) upon receipt and 
            inspection of the returned product.
          </p>
        </#if>
      </li>
      <li id="complaints" class="list-group-item">
        <#if (locale == "hr")>
          <h4>6. Reklamacije, garancija i servis</h4>
          <p>
            Reklamacije primamo telefonom (<a href="tel:38517999194">+385 (0)1 7999 194</a>) i mailom 
            (<a href="mailto:diykits.shop@mikrotron.hr">diykits.shop@mikrotron.hr</a>). Kako bi smo ubrzali postupak, molimo pripremite broj
            narudžbenice ili računa prije javljanja. U slučaju da je pošiljka oštećena u transportu, a oštećenja su vidljiva
            pri preuzimanju, i molimo Vas da oštećenu pošiljku ne preuzimate. Za elektroničke komponente, sklopove i kitove 
            namjenjene samogradnji nema jamstvenog roka, tj. garancija važi samo za prvu uporabu. Ukoliko prilikom
            prve uporabe ustanovite da je uređaj neispravan, javite nam se na neki od gore navedenih načina kako bi dogovorili
            zamjenu za ispravan uređaj. Mikrotron u pravilu ne vrši servisiranje elektroničkih komponenti. Iznimke su moguće -
            kontaktirajte nas mailom ili telefonom.
          </p>
        <#else>
          <h4>6. Complaints, Warranty and Service</h4>
          <p>
            We accept complaints by phone (<a href="tel:38517999194">+385 (0)1 7999 194</a>) and by email 
            (<a href="mailto:diykits.shop@mikrotron.hr">diykits.shop@mikrotron.hr</a>). In order to speed up the process, please prepare the purchase 
            order number or invoice number before contacting us. In the event that the shipment is damaged during transport, and the 
            damage is visible upon pickup, please do not pick up the damaged shipment. There is no warranty period for electronic 
            components, assemblies and kits intended for self-assembly, i.e. the warranty is only valid for the first use. 
            If during the first use you find that the device is defective, contact us in one of the above-mentioned ways in order to 
            arrange a replacement for a correct device. As a rule, Mikrotron does not service electronic components. 
            Exceptions are possible - contact us by email or phone.
          </p>
        </#if>
      </li>
      <li id="privacy" class="list-group-item">
        <#if (locale == "hr")>
          <h4>7. Izjava o privatnosti</h4>
          <p>
            Izjava o privatnosti odnosi se na povjerljivost osobnih podataka koji se
            prikupljaju za potrebe korištenja web trgovine i sastavni je dio <b>Općih
            uvjeta poslovanja</b>. Mikrotron prikuplja osobne podatke o korisnicima
            web trgovine u svrhu izdavanja dokumenata vezanih uz proces prodaje
            (narudžbenica i račun) i u svrhu isporuke robe.
          </p>
          <p>
            Osobnim podacima pristupa se isključivo preko enkriptiranih komunikacijskih
            kanala (HTTPS). Odgovornost je svakog korisnika upisati točne podatke pri
            prijavi u trgovinu, i u slučaju promjene, održavati podatke ažurnima.
            Registracija u web trgovini moguća je samo za osobe starije od 16 godina,
            ukoliko ustanovimo da je registrirani korisnik mlađi od 16 godina, odmah
            ćemo obrisati sve osobne podatke tog korisnika. Upisani osobni podaci
            ispisuju se na narudžbenici i računu, koji se kupcu šalju elektroničkom
            poštom automatski, ili u iznimnim slučajevima ručno. Sigurnost odredišnog
            sandučića i poslužitelja elektronske pošte odgovornost je kupca.
          </p>
          <p>
            Osobni podaci kupaca čuvaju se na serveru koji se nalazi na teritoriju EU
            i zaštićeni su od neovlaštenog pristupa. Mikrotron može podijeliti osobne
            podatke kupca (ime i prezime, adresu i broj telefona) sa pružateljima usluge
            isporuke kao i sa poduzećem s kojim Mikrotron ima sklopljen ugovor o obavljanju
            knjigovodstvenih i računovodstvenih usluga. Osim u navedenim slučajevima ili
            u slučaju propisanim zakonom (npr. sudski nalog), Mikrotron ne dijeli podatke
            s trećim osobama. Osobni podaci korisnika čuvaju se u skladu s hrvatskim
            zakonskim propisima o roku čuvanja određene dokumentacije.
          </p>
          <p>
            Svaki registrirani korisnik ima pravo na potpuni i bezuvjetni uvid u sve
            prikupljene osobne podatke koje se odnose na tog korisnika, njihovu obradu
            i korištenje. Pravo se ostvaruje pisanim zahtjevom na e-mail <a href="mailto:diykits.shop@mikrotron.hr">
            diykits.shop@mikrotron.hr</a> poslanim s e-mail adrese koju je korisnik koristio
            prilikom registracije u web trgovini. Na isti način registrirani korisnik
            može zatražiti i ispravak osobnih podataka, kao i brisanje svih osobnih
            podataka. Brisanje osobnih podataka Mikrotron može u potpunosti ili djelomično
            odbiti uz pisano obrazloženje ukoliko bi to brisanje bilo u suprotnosti s
            trenutno važećim hrvatskim zakonskim i podzakonskim aktima o čuvanju
            određene vrste dokumentacije (npr. računa).
          </p>
          <p>
            Ukoliko smatrate da smo na bilo koji način prekršili važeće zakonske odredbe
            koje se tiču privatnosti i prikupljanja i obrade osobnih podataka, prigovor
            možete podnijeti na e-mail adresu <a href="mailto:diykits.shop@mikrotron.hr">diykits.shop@mikrotron.hr</a>.
            Ako iz bilo kojeg razloga niste zadovoljni riješenjem Vašeg prigovora, imate
            pravo uložiti pritužbu Agenciji za zaštitu osobnih podataka, a od 25. svibnja
            2018. godine i nadzornom tijelu unutar EU-a.
          </p>
          <p id="cookies">
            Mikrotron web trgovina koristi tzv. 'kolačiće' (eng. cookies) za osiguravanje
            pune funkcionalnosti, ukoliko isključite korištenje 'kolačića' u Vašem web
            pregledniku, web trgovina neće biti u stanju funkcionirati kako je
            predviđeno te ju nećete moći koristiti za naručivanje roba i usluga.
            U sklopu web trgovine postoje linkovi koji vode na druge web stranice,
            Mikrotron ne odgovara za sadržaj i pravila privatnosti tih stranica.
          </p>
          <p>
            Ovu Izjavu o privatnosti Mikrotron može izmijeniti u bez prethodne obavijesti,
            a obavijest o izmjeni biti će istaknuta na početnoj stranici web trgovine u
            trajanju od 30 dana. Mikrotron može koristiti i druge načine obavještavanja
            korisnika (npr. putem društvenih mreža) o izmjeni ove Izjave, ukoliko smatra
            da je to potrebno. Korisnici se pozivaju da pročitaju izmjene i ako se s njima
            ne slažu da ne koriste usluge ove web trgovine.
          </p>
        <#else>
          <h4>7. Privacy Statement</h4>
          <p>
            This statement addresses privacy of personal data collected and processed in
            use of this web shop and is an integral part of Terms and Conditions. Mikrotron
            collects web shop users personal data needed for the implementation of the online
            sales process and shipping of the purchased goods.
          </p>
          <p>
            Personal data is accessed only via encrypted chanells (HTTPS). User's sole
            responsibility is to enter valid data, and maintain them in case of any
            changes. Only persons over 16 years of age are allowed to register. If Mikrotron
            finds out about any person under 16 years of age being registered, Mikrotron
            will erase all personal data for that person.
          </p>
          <p>
            Personal data is being used for order form and invoice and are send to user
            by e-mail automatically or manually. Security of destination mailbox and
            mail server are sole responsibility of user.
          </p>
          <p>
            User personal date is being kept on a EU based server(s) and can be accessed
            by authorized personnel only. Mikrotron can share personal data needed for
            the shipment of the goods (name, address, phone number or e-mail address)
            with the post office or the delivery company. Also, personal data will be
            shared with the authorized accounting company for the accounting purposes
            only. Apart from the cases stated, your personal data will not be shared
            with anyone else. Your personal data may be shared with the third party when
            obligated by the applicable law. Personal data is being kept at least for
            the period prescribed by the Croatian applicable laws.
          </p>
          <p>
            Every registered user has the right to full and unlimited report on the
            collection, processing and personal data usage for the user in question.
            This right can be carried out by sending an e-mail to <a
            href="mailto:diykits.shop@mikrotron.hr">diykits.shop@mikrotron.hr</a> from the registered
            e-mail address. This contact can be used to require personal data
            corrections or the deletion of personal data. Mikrotron can decline
            deletion fully or partially when the request is in the opposition of
            applicable laws.
          </p>
          <p>
            If you feel that we're in any way misusing your privacy rights, you can
            send us formal complaint by sending an e-mail to <a
            href="mailto:diykits.shop@mikrotron.hr">diykits.shop@mikrotron.hr</a> from the registered
            e-mail address and we'll get back to you. If you're in any way
            dissatisfied with the answer you have the right to the EU supervisory
            agency as of May 25, 2018.
          </p>
          <p id="cookies">
            Mikrotron web shop uses cookies for the full web shop functionality - if
            you turn of cookies in your web browser, web shop will not have full
            functionality and you might not be able to complete an order in the web
            shop. Web shop may contain links to the other web sites, Mikrotron is in 
            no way responsible for those site's content and privacy policy.
          </p>
          <p>
            This Privacy Statement can be changed by Mikrotron with no prior notice,
            users will be informed of the change on the web shop main page. This notice
            will be visible for at least 30 days from the day of change. Mikrotron can
            use other means of communication (e.g. social media) to inform users of the
            change, if necessary. Users are invited to read this Statement in full, and
            if they don't agree with any part of it, to stop using this web shop.
          </p>
        </#if>
      </li>
    </ul>
  </div>
</div>