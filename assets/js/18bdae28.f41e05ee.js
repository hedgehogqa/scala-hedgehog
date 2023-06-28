"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[376],{3905:(e,t,n)=>{n.d(t,{Zo:()=>u,kt:()=>m});var r=n(7294);function i(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function o(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function a(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?o(Object(n),!0).forEach((function(t){i(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):o(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function s(e,t){if(null==e)return{};var n,r,i=function(e,t){if(null==e)return{};var n,r,i={},o=Object.keys(e);for(r=0;r<o.length;r++)n=o[r],t.indexOf(n)>=0||(i[n]=e[n]);return i}(e,t);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(r=0;r<o.length;r++)n=o[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(i[n]=e[n])}return i}var l=r.createContext({}),p=function(e){var t=r.useContext(l),n=t;return e&&(n="function"==typeof e?e(t):a(a({},t),e)),n},u=function(e){var t=p(e.components);return r.createElement(l.Provider,{value:t},e.children)},d="mdxType",g={inlineCode:"code",wrapper:function(e){var t=e.children;return r.createElement(r.Fragment,{},t)}},c=r.forwardRef((function(e,t){var n=e.components,i=e.mdxType,o=e.originalType,l=e.parentName,u=s(e,["components","mdxType","originalType","parentName"]),d=p(n),c=i,m=d["".concat(l,".").concat(c)]||d[c]||g[c]||o;return n?r.createElement(m,a(a({ref:t},u),{},{components:n})):r.createElement(m,a({ref:t},u))}));function m(e,t){var n=arguments,i=t&&t.mdxType;if("string"==typeof e||i){var o=n.length,a=new Array(o);a[0]=c;var s={};for(var l in t)hasOwnProperty.call(t,l)&&(s[l]=t[l]);s.originalType=e,s[d]="string"==typeof e?e:i,a[1]=s;for(var p=2;p<o;p++)a[p]=n[p];return r.createElement.apply(null,a)}return r.createElement.apply(null,n)}c.displayName="MDXCreateElement"},5235:(e,t,n)=>{n.r(t),n.d(t,{assets:()=>l,contentTitle:()=>a,default:()=>g,frontMatter:()=>o,metadata:()=>s,toc:()=>p});var r=n(7462),i=(n(7294),n(3905));const o={title:"Integration with MUnit",sidebar_position:2,sidebar_label:"MUnit",slug:"/integration-munit"},a=void 0,s={unversionedId:"integration/munit",id:"integration/munit",title:"Integration with MUnit",description:"MUnit",source:"@site/../generated-docs/target/mdoc/integration/munit.md",sourceDirName:"integration",slug:"/integration-munit",permalink:"/scala-hedgehog/docs/integration-munit",draft:!1,tags:[],version:"current",sidebarPosition:2,frontMatter:{title:"Integration with MUnit",sidebar_position:2,sidebar_label:"MUnit",slug:"/integration-munit"},sidebar:"tutorialSidebar",previous:{title:"Minitest",permalink:"/scala-hedgehog/docs/integration-minitest"}},l={},p=[{value:"MUnit",id:"munit",level:2}],u={toc:p},d="wrapper";function g(e){let{components:t,...n}=e;return(0,i.kt)(d,(0,r.Z)({},u,n,{components:t,mdxType:"MDXLayout"}),(0,i.kt)("h2",{id:"munit"},"MUnit"),(0,i.kt)("p",null,"Scala Hedgehog provides an integration module for ",(0,i.kt)("a",{parentName:"p",href:"https://scalameta.org/munit/"},"munit"),". This allows you to define property-based and example-based Hedgehog tests within a munit test suite. If you use this integration, you won't need to Scala Hedgehog sbt testing extension, because you're using the one provided by munit:"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},'val hedgehogVersion = "0.10.1"\nlibraryDependencies += "qa.hedgehog" %% "hedgehog-munit" % hedgehogVersion % Test\n')),(0,i.kt)("admonition",{title:"NOTE",type:"info"},(0,i.kt)("p",{parentName:"admonition"},"If you're using sbt version ",(0,i.kt)("inlineCode",{parentName:"p"},"1.9.0")," or ",(0,i.kt)("strong",{parentName:"p"},"lower"),", you need to add the following line to your ",(0,i.kt)("inlineCode",{parentName:"p"},"build.sbt")," file:"),(0,i.kt)("pre",{parentName:"admonition"},(0,i.kt)("code",{parentName:"pre",className:"language-scala"},'testFrameworks += TestFramework("hedgehog.sbt.Framework")\n'))),(0,i.kt)("admonition",{title:"NOTE",type:"info"},(0,i.kt)("p",{parentName:"admonition"},"For sbt version ",(0,i.kt)("inlineCode",{parentName:"p"},"1.9.1")," or ",(0,i.kt)("strong",{parentName:"p"},"higher"),", this step is not necessary, as ",(0,i.kt)("a",{parentName:"p",href:"https://github.com/sbt/sbt/pull/7287"},"Hedgehog is supported by default"),".")),(0,i.kt)("p",null,"Here's an example of using ",(0,i.kt)("inlineCode",{parentName:"p"},"hedgehog-munit"),":"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},'import hedgehog.munit.HedgehogSuite\nimport hedgehog._\n\nclass ReverseSuite extends HedgehogSuite {\n  property("reverse alphabetic strings") {\n    for {\n      xs <- Gen.alpha.list(Range.linear(0, 100)).forAll\n    } yield assertEquals(xs.reverse.reverse, xs)\n  }\n  \n  test("reverse hello") {\n    withMunitAssertions{ assertions =>\n      assertions.assertEquals("hello".reverse, "olleh")\n    }\n    "hello".reverse ==== "olleh"\n  }\n}\n')),(0,i.kt)("p",null,(0,i.kt)("inlineCode",{parentName:"p"},"HedgehogSuite")," provides ",(0,i.kt)("inlineCode",{parentName:"p"},"munit"),"-like assertions, along with all the ",(0,i.kt)("inlineCode",{parentName:"p"},"hedgehog.Result")," methods and members, that return results in the standard hedgehog report format while satisfying munit's exception-based test failures."))}g.isMDXComponent=!0}}]);