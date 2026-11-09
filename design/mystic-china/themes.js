'use strict';
const themes = [
  {id:'china-red',name:'中国红',en:'Red celebration',mood:'喜庆 · 团圆 · 节日',description:'正红定调，米白留出阅读空间，淡金点亮细节。喜庆，也耐看。',colors:{
    primary:'#B82025',deep:'#781B20',secondary:'#356B59',paper:'#FFF7EE',surface:'#FFFDFA',ink:'#352323',muted:'#765B56',border:'#DDC8BC',
    'primary-soft':'#F8E5DE','secondary-soft':'#E8EEE3',gold:'#805411','gold-soft':'#F6E9CC',error:'#A01D43','error-soft':'#FAE3EB',success:'#2D654B','success-soft':'#E7F0E6',
    'on-primary':'#FFF9EF','on-deep':'#FFF4DD','on-deep-muted':'#EBCDC1',stamp:'#AD281F',art:'#C5967E',focus:'#356B59'}},
  {id:'porcelain-blue',name:'青花蓝',en:'Porcelain blue',mood:'典雅 · 图鉴 · 阅读',description:'青花瓷的蓝，配月白与玉青。清爽、安静，适合日常发现与阅读。',colors:{
    primary:'#234E89',deep:'#17365D',secondary:'#38766A',paper:'#F7F9FA',surface:'#FFFFFF',ink:'#192D40',muted:'#5B6E80',border:'#CDD9E4',
    'primary-soft':'#E8EFF7','secondary-soft':'#E7F1EC',gold:'#805D21','gold-soft':'#F7EDDA',error:'#A8343B','error-soft':'#F9E5E6',success:'#29664E','success-soft':'#E6F0E9',
    'on-primary':'#FFFFFF','on-deep':'#F7FBFF','on-deep-muted':'#C8D8EB',stamp:'#B4443F',art:'#8DA5BD',focus:'#234E89'}},
  {id:'ink-night',name:'墨夜金',en:'Ink & gold',mood:'深色 · 沉静 · 夜读',description:'墨黑承托暖金，浅杏文字配柔青点缀。为夜间阅读留一点温柔的光。',colors:{
    primary:'#E3B86B',deep:'#302B23',secondary:'#91C4B0',paper:'#1B1F23',surface:'#25292E',ink:'#F0E9DE',muted:'#B6BDC5',border:'#535A63',
    'primary-soft':'#3A3226','secondary-soft':'#263C34',gold:'#E4BE74','gold-soft':'#3A3121',error:'#FFB4BC','error-soft':'#4A262E',success:'#94D2AE','success-soft':'#263D30',
    'on-primary':'#231F18','on-deep':'#F6EAD3','on-deep-muted':'#D4C9B6',stamp:'#F0A08E',art:'#877755',focus:'#E3B86B'}}
];
function themeChoices(compact=false){
  return themes.map(t=>`<button type="button" class="theme-choice${compact?' compact':''}" data-theme-choice="${t.id}" aria-pressed="${document.documentElement.dataset.theme===t.id}" style="--choice-primary:${t.colors.primary};--choice-paper:${t.colors.paper};--choice-ink:${t.colors.ink};--choice-secondary:${t.colors.secondary};--choice-gold:${t.colors.gold}"><span class="theme-sample" aria-hidden="true"><i></i><i></i><i></i><b>中</b></span><span class="theme-choice-text"><strong>${compact?t.en:t.name}</strong><small>${compact?t.name:t.en}</small><em>${t.mood}</em></span><span class="theme-selection" aria-hidden="true">✓</span></button>`).join('');
}
function applyTheme(id){
  const theme=themes.find(t=>t.id===id)||themes[0];
  document.documentElement.dataset.theme=theme.id;
  for(const [key,value] of Object.entries(theme.colors))document.documentElement.style.setProperty(`--theme-${key}`,value);
  document.querySelectorAll('[data-theme-choice]').forEach(button=>button.setAttribute('aria-pressed',String(button.dataset.themeChoice===theme.id)));
  document.querySelectorAll('[data-theme-name]').forEach(el=>el.textContent=`${theme.en} · ${theme.name}`);
  document.querySelector('#theme-description').textContent=theme.description;
  try{localStorage.setItem('mysticchina-design-theme',theme.id);}catch{}
}
function themeLabel(){const t=themes.find(t=>t.id===document.documentElement.dataset.theme);return `${t.en} · ${t.name}`;}
function themeGuide(){return `<table><thead><tr><th>主题 / 气质</th><th>主色</th><th>辅助色</th><th>金色点缀</th><th>阅读底色</th><th>正文</th></tr></thead><tbody>${themes.map(t=>`<tr><td><strong>${t.name}</strong><br>${t.mood}</td>${['primary','secondary','gold','paper','ink'].map(role=>`<td><span class="guide-colour" style="background:${t.colors[role]}"></span><code>${t.colors[role]}</code></td>`).join('')}</tr>`).join('')}</tbody></table><p>中国红使用约 70% 暖色留白、25% 红色导航与主操作、5% 金色细节的视觉层级。正文保持深褐字，金色不承担长文正文。灾难纪念页保留克制排版，不增加庆祝装饰。主题切换不改变售价、审核状态或资产含义。</p>`;}
let savedTheme='china-red';
try{savedTheme=localStorage.getItem('mysticchina-design-theme')||savedTheme;}catch{}
applyTheme(savedTheme);
document.querySelector('#theme-picker').innerHTML=themeChoices();
document.addEventListener('click',event=>{const choice=event.target.closest('[data-theme-choice]');if(choice)applyTheme(choice.dataset.themeChoice);});
