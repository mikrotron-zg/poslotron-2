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
    <b>${uiLabelMap.EcommerceFaqFull}</b>
  </div>

  <div class="card-body text-secondary">
    <div>
      <#if (locale == "hr")>
        <ul class="list-unstyled">
          <a href="#cash"><li class="mb-2">Može keš?</li></a>
          <a href="#payment"><li class="mb-2">Kako da platim?</li></a>
          <a href="#card"><li class="mb-2">Zašto ne mogu platiti karticom?</li></a>
          <a href="#pickup"><li class="mb-2">Kako da osobno preumem robu?</li></a>
          <a href="#buy"><li class="mb-2">Kako da kupim? (kupnja: fizičke osobe)</li></a>
          <a href="#buy2"><li class="mb-2">Ponuda ili narudžba? (kupnja: pravne osobe)</li></a>
          <a href="#delivery"><li class="mb-2">Koje su cijene i rokovi isporuke?</li></a>
          <a href="#HP"><li class="mb-2">Koju vrstu pošiljke šaljete kada izaberem stavku dostave
             'Hrvatska pošta'?</li></a>
          <a href="#cod"><li class="mb-2">Zašto ne mogu platiti kod prijema isporuke (pouzećem)?</li></a>
          <a href="#reg"><li class="mb-2">Zašto se moram registrirati?</li></a>
          <a href="#rejected"><li class="mb-2">Zašto je moja narudžba odbijena?</li></a>
          <a href="#mail"><li class="mb-2">Zašto ne odgovarate na upite e-mailom?</li></a>
        </ul>
      <#else>
        <ul class="list-unstyled">
          <a href="#payment"><li class="mb-2">How do I pay?</li></a>
          <a href="#card"><li class="mb-2">Why don't you accept credit cards?</li></a>
          <a href="#delivery"><li class="mb-2">What are shipping prices and terms?</li></a>
          <a href="#reg"><li class="mb-2">Why do I have to register?</li></a>
        </ul>
      </#if>
    </div>
    <ul class="list-group list-group-flush">
      <#if (locale == "hr")>
        <li id="cash" class="list-group-item">
          <h4>Može keš?</h4>
          <p>NE MOŽE!</p>
          <p>
            Fiskalizacija, hvala, ne treba. Iako smo na tržnici, nemamo ni dućan ni blagajnu, nego samo ured.
            Ako nam baš banete na vrata, vjerojatno na pametnom telefonu imate nešto manje pametno mobilno
            bankarstvo pa sve možemo riješiti na licu mjesta: mi napravimo ponudu/narudžbu, vi napravite
            uplatu i pošaljete potvrdu, mi izdamo račun i robu - i svi sretni i zadovoljni.
          <p>
          </p>
            Ako baš hoćete hardcore verziju, imate preko puta RBA, na ćošku PBZ, malo dalje ZABA, još malo
            dalje pošta: pokažete im narudžbenicu koju isprintamo i iskeširate se na šalteru.
          </p>
        </li>
      </#if>
      <li id="payment" class="list-group-item">
        <#if (locale == "hr")>
          <h4>Kako da platim?</h4>
          <p>
            Uplatite na račun firme: <b>HR8023400091110675464</b>. Ovaj IBAN možete naći u zaglavlju narudžbenice 
            (klik: PDF) i na stranici <a href="<@ofbizUrl>aboutus</@ofbizUrl>">O nama</a>. U model upišite 
            <b>HR00</b>, a u poziv na broj primatelja upišite broj narudžbe (nama to ne treba, ali neke 
            bankarske aplikacije na tome inzistiraju). Alternativno, ako plaćate putem mobilnog bankarstva, 
            možete koristiti 2D barkod koji se prikazuje kod potvrđivanja narudžbe i u PDF-u narudžbenice.
          </p>
        <#else>
          <h4>How do I pay?</h4>
          <p>
            Paypal, or wire transfer. Just select appropriate payment method during checkout process. Order PDF
            available after checkout contains all the data required for wire transfer - our bank routing number and IBAN.
          </p>
        </#if>
      </li>
      <li id="card" class="list-group-item">
        <#if (locale == "hr")>
          <h4>Zašto ne mogu platiti karticom?</h4>
          <p>
            Kartično poslovanje naplaćuje se trgovcu, pa bi morali bi dići cijene (ne sviđa nam se) ili imati različite cijene 
            za razne vrste plaćanja (ovo je neka vrsta diskriminacije i još manje nam se sviđa od opcije broj jedan). 
            Također, kartice sa sobom donose i (nezanemariv) sigurnosni rizik.
            S obzirom da danas skoro pa svatko ima internet na mobitelu, a praktički sve banke nude i internet bankarstvo 
            i mobilno bankarstvo, krajnje je vrijeme da kartice pošaljemo tamo gdje im je i mjesto.
          </p>
        <#else>
          <h4>Why don't you accept credit cards?</h4>
          <p>
             Payment gateways request additional charges. Long story short, our prices would have to go up (don't like that) or
             we'd have to have different prices based on payment type (don't like that either). Credit cards also bring security 
             risks along.
          </p>
        </#if>
      </li>
      <#if (locale == "hr")>
        <li id="pickup" class="list-group-item">
          <h4>Kako da osobno preumem robu?</h4>
          <ol>
            <li>Registrirajte se u dućan (Registracija: Osoba)</li>
            <li>Popunite košaru i kliknite <b>Napravi narudžbu</b></li>
            <li>Kod potvrde narudžbe, izaberite <b>osobno preuzimanje u uredu</b></li>
            <li>Potvrdite narudžbu</li>
            <li>Platite narudžbu</li>
            <li>Vidimo se u uredu! Ako narudžbu podižete isti dan, ne zaboravite ponijeti kopiju uplatnice 
            (ili ju pošaljite na email <a href="mailto:shop@diykits.eu">shop@diykits.eu</a>) ;)</li>
          </ol>
        </li>
        <li id="buy" class="list-group-item">
          <h4>Kako da kupim?</h4><h5>(kupnja: fizičke osobe)</h5>
          <ol>
            <li>Registrirajte se u dućan (Registracija: Osoba)</li>
            <li>Popunite košaru i kliknite <b>Napravi narudžbu</b></li>
            <li>Potvrdite narudžbu</li>
            <li>Platite narudžbu</li>
          </ol>
          I to je to - roba stiže na adresu (ili dođete po nju u naš ured, vidi gore).<br/>
          Ako ne plaćate u PBZ-u, uplatu dobijemo kasnije ili tek sutra pa se može dogoditi da  neće biti poslana 
          isti dan. U tom slučaju, isporuku možete skratiti za jedan dan ako nam pošaljete potvrdu o uplati na email
          <a href="mailto:shop@diykits.eu">shop@diykits.eu</a>.
        </li>
        <li id="buy2" class="list-group-item">
          <h4>Ponuda ili narudžba?</h4><h5>(kupnja: pravne osobe)</h5>
          <p>
            Nama je u stvari svejedno, nakon što se registrirate kao pravna osoba, popunite košaru i kliknete na 
            <b>Napravi narudžbu</b>, možete napraviti uplatu prema toj narudžbi i mi ćemo vam izdati račun na 
            firmu (onaj čuveni <b>R1</b> koji već godinama ne postoji, ali svi ga tako zovemo).
          </p>
          <p>
            Ako procedura u vašoj firmi/ustanovi/instituciji zahtjeva ponudu, onda nakon što natovarite košaricu
            sa svim onim lijepim stvarčicama koje vam trebaju, kliknite na <b>Ponuda na firmu</b> i dobiti ćete 
            na e-mail potvrdu s brojem zahtjeva, a uskoro će stići i naša ponuda (ako se to ne dogodi, provjerite
            da nije slučajno završila u spamu). Nakon što zaprimimo uplatu po ponudi, ostaje samo da čekate dostavljača
            ili dođete do nas po robu, zavisno koju opciju odaberete.
          </p>
        </li>
      </#if>
      <li id="delivery" class="list-group-item">
        <#if (locale == "hr")>
          <h4>Koje su cijene i rokovi isporuke?</h4>
          <p>
            Cijena isporuke računa se prema težini, uključujući ambalažu, na osnovu cijena dostave i cijena ambalaže. 
            Izračunata cijena prikazuje se kada u narudžbi izaberete adresu dostave. Okvirno, cijene isporuke su:
            <ul class="pl-lg-5">
              <li><b>Hrvatska pošta</b>: od 1.33 € (10.00 kn) naviše</li>
              <li><b>GLS</b>: od 5,44 € (41.00 kn) naviše</li>
            </ul>
          </p>
          <p>
            Za uplate vidljive na našem računu (ili ako pošaljete potvrdu na email) do 12 sati
            pošiljku predajemo na dostavu do kraja dana. Ne brinite ako ste zakasnili, u 99%
            slučajeva i za uplate iza 12 sati isporuka kreće isti dan :) Rokovi isporuke su:
            <ul class="pl-lg-5">
              <li><b>Hrvatska pošta</b>: Vrste pošiljki i rokovi dostave detaljnije su objašnjeni <a href="#HP">ovdje</a></li>
              <li><b>GLS</b>: idući radni dan nakon zaprimanja pošiljke (iznimno dva, izuzetci su nabrojani na GLS-ovoj web stranici)</li>
            </ul>
          </p>
          <p>
            Ako neki naručeni artikl trenutno nemamo u dućanu, rokove isporuke javljamo e-mailom ili telefonski. U tom slučaju, 
            po dogovoru, možete poništiti narudžbu u cijelosti ili djelomično, a cijenu otkazane robe refundiramo odmah.
          </p>
        <#else>
          <h4>What are shipping prices and terms?</h4>
          <p>
            Shipping prices are calculated automatically when you make an order. At this moment, shipping by post is the only option,
            but this should be updated shortly. Your order will be sent as a registered letter if under 2 kg, or as an international
            package if over 2 kg.
          </p>
          <p>
            Currently we ship to EU countries only. All EU countries are supported. EU is divided into 4 zones, and shipping may last 
            3 to 10 days, depending on zone. Please contact us if shipping options do not display. Goods we have on stock are 
            shipped on daily basis.
          </p>
        </#if>
      </li>
      <#if (locale == "hr")>
        <li id="HP" class="list-group-item">
          <h4>Koju vrstu pošiljke šaljete kada izaberem stavku dostave 'Hrvatska pošta'?</h4>
          <p>
            Kako bi dobili optimalnu (ako je to ikako moguće) poštansku dostavu bez previše stavki kod
            izbora dostave i uz najmanje moguće troškove, isporuka poštom podjeljena je u tri kategorije,
            ovisno o težini pošiljke:
            <ol class="pl-lg-5">
              <li class="mb-1">
                Pošiljke <b>do 250 g</b> šalju se kao prioritetno pismo. Obično pismo više ne šaljemo
                jer je znalo putovati i do dva tjedna. Prema tvrdnjama pošte 85% ovih pošiljaka uručuje se
                za jedan radni dan (D+1), 95% za dva (D+2), a sve ostale za tri (D+3). Za otoke, svi rokovi su duži za jedan dan.
                Pomnožite navedene rokove s faktorom 2 - 4 pa je to onda bliže stvarnosti.
              </li>
              <li class="mb-1">
                Pošiljke u rasponu <b>od 251-1000 g</b> šaljemo kao preporučenu pošiljku. Iznimno, pošiljku do
                250 g ćemo također poslati kao preporučenu pošiljku ako joj vrijednost prelazi 25.00 € (188.36 kn). Razlika u
                cijeni u tom slučaju ide na naš trošak. Rok dostave = D+3 puta faktor iz točke 1)
              </li>
              <li>
                Pošiljke <b>teže od 2 kg</b> šaljemo kao paket s dostavom na adresu, tamo gdje je takva vrsta
                dostave moguća (
                <a target="_blank" href="https://www.posta.hr/UserDocsImages/posta/Dokumenti/Popis_NASELJA_dostava_paketa_(22.01.2018).pdf">
                Popis mjesta s dostavom na adresu</a> - jako je kratak i stalno se smanjuje).
                Tamo gdje nije, nažalost morati ćete otići po Vašu pošiljku u poštanski ured. Rok dostave za pakete je
                tri radna dana (D+3). Nažalost, ova usluga je u zadnjih par godina enormno poskupila pa je za ovu vrstu dostave
                GLS najjeftinija opcija.
              </li>
            </ol>
          </p>
          <p>
            Vrijedi napomenuti da za pakete koji su uručeni s prekoračenjem roka dostave, možete pokrenuti
            <a target="_blank" href="https://www.posta.hr/reklamacije-275/275">Potražni postupak</a> (iako
            zapravo znate gdje je pošiljka, pošta ne poznaje pojam žalbe ili reklamacije), a na isti način
            možete se žaliti i na bilo kakve druge nepravilnosti u isporuci. Ako Vam uvaže žalbu, imate pravo i
            na naknadu u iznosu trostruke poštarine :)
          </p>
        </li>
        <li id="cod" class="list-group-item">
          <h4>Zašto ne mogu platiti kod prijema isporuke (pouzećem)?</h4>
          <p>
            Pouzeće je dodatni trošak i nepouzdan sustav naplate: ako Vas poštar
            ne nađe doma, a do pošte ne odete u tjedan dana, gubimo novac. S obzirom na
            (ne)pouzdanost pošte, gubitak je garantiran.
          </p>
        </li>
      </#if>
      <li id="reg" class="list-group-item">
        <#if (locale == "hr")>
          <h4>Zašto se moram registrirati?</h4>
          <p>
            Vaši osnovni podaci trebaju nam za izdavanje računa, dostavu i da Vas možemo kontaktirati ako nešto
            ne možemo isporučiti odmah ili u slučaju bilo kakvih drugih problema. Ne brinite, vaše podatke čuvamo
            kao oko u glavi, ne dijelimo ih ni sa kime i pristupiti im možemo samo mi. Ako ste i dalje zabrinuti,
            provjerite našu <a href="<@ofbizUrl>policies</@ofbizUrl>#privacy">Izjavu o privatnosti</a>.
          </p>
        <#else>
          <h4>Why do I have to register?</h4>
          <p>
            We need your basic data for invoicing, delivery and to be able to contact you if we cannot deliver 
            some product immediately or in case of any other problems. Don't worry, we keep your data safe, we don't 
            share it with anyone and only we can access it. If you are still concerned, please check our 
            <a href="<@ofbizUrl>policies</@ofbizUrl>#privacy">Privacy Statement</a>.
          </p>
        </#if>
      </li>
      <#if (locale == "hr")>
        <li id="rejected" class="list-group-item">
          <h4>Zašto je moja narudžba odbijena?</h4>
          <p>
            Narudžbe koje nisu realizirane u roku od dva tjedna, poništavaju se, osim
            ako se nismo drugačije dogovorili ;)
          </p>
        </li>
        <li id="mail" class="list-group-item">
           <h4>Zašto ne odgovarate na upite e-mailom?</h4>
           <p>
              Odgovaramo - provjerite svoj spam folder (neželjena pošta). Sasvim je moguće da će naše poruke biti 
              označene kao neželjena pošta, jer sadrže riječi koje koriste spammeri - ponuda, narudžba, račun...
              Gmail/Outlook/Hotmail korisnici to mogu izbjeći tako da našu mail adresu dodaju u kontaktnu listu.
            </p>
        </li>
      </#if>
    </ul>
  </div>
</div>